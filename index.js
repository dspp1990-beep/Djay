// Smart Irrigation – live sync server (Cloudflare Worker + one SQLite-backed Durable Object).
//
// Every phone keeps its own local copy and works offline. When online it:
//   * opens a WebSocket to /ws  -> receives every new valve record / shift change instantly
//   * POSTs its unsent records to /logs (idempotent by uuid)
//   * GETs /logs?since=<seq> after (re)connecting to catch up on anything it missed
//
// Auth: one shared team key (Worker secret TEAM_KEY), sent as header "X-Team-Key" or ?key= for the WebSocket.

import { DurableObject } from "cloudflare:workers";

const JSON_HEADERS = { "content-type": "application/json; charset=utf-8" };
const json = (obj, status = 200) => new Response(JSON.stringify(obj), { status, headers: JSON_HEADERS });

function authorised(request, env, url) {
  const key = request.headers.get("x-team-key") || url.searchParams.get("key") || "";
  return env.TEAM_KEY && key.length > 0 && key === env.TEAM_KEY;
}

export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    if (url.pathname === "/" || url.pathname === "/health") {
      return json({ ok: true, service: "smart-irrigation-sync", configured: Boolean(env.TEAM_KEY) });
    }
    if (!env.TEAM_KEY) return json({ error: "Server not configured: TEAM_KEY secret missing" }, 500);
    if (!authorised(request, env, url)) return json({ error: "Wrong or missing team key" }, 401);
    // One shared "room" for the whole farm
    const stub = env.SYNC.get(env.SYNC.idFromName("sardoba"));
    return stub.fetch(request);
  },
};

export class SyncRoom extends DurableObject {
  constructor(ctx, env) {
    super(ctx, env);
    this.sql = ctx.storage.sql;
    this.sql.exec(`CREATE TABLE IF NOT EXISTS valve_log (
      seq        INTEGER PRIMARY KEY AUTOINCREMENT,
      uuid       TEXT UNIQUE NOT NULL,
      ts         INTEGER NOT NULL,
      tag        TEXT NOT NULL,
      action     TEXT NOT NULL,
      system     INTEGER,
      ph         TEXT,
      block      TEXT,
      area_ha    REAL,
      operator   TEXT,
      note       TEXT,
      lat        REAL,
      lon        REAL,
      dist_m     REAL,
      device     TEXT,
      deleted    INTEGER NOT NULL DEFAULT 0,
      received   INTEGER NOT NULL
    )`);
    this.sql.exec(`CREATE TABLE IF NOT EXISTS valve_shift (
      tag      TEXT PRIMARY KEY,
      shift    INTEGER NOT NULL,
      seq      INTEGER NOT NULL,
      operator TEXT,
      updated  INTEGER NOT NULL
    )`);
    this.sql.exec(`CREATE TABLE IF NOT EXISTS meta (k TEXT PRIMARY KEY, v INTEGER NOT NULL)`);
  }

  // A single increasing counter shared by log rows and shift changes, so "since" works for both.
  nextSeq() {
    const row = this.sql.exec(`SELECT v FROM meta WHERE k='seq'`).toArray()[0];
    const next = (row ? row.v : 0) + 1;
    this.sql.exec(`INSERT INTO meta (k, v) VALUES ('seq', ?) ON CONFLICT(k) DO UPDATE SET v=excluded.v`, next);
    return next;
  }

  rowToEvent(r) {
    return {
      type: "log", seq: r.seq, uuid: r.uuid, ts: r.ts, tag: r.tag, action: r.action, system: r.system,
      ph: r.ph, block: r.block, areaHa: r.area_ha, operator: r.operator, note: r.note,
      lat: r.lat, lon: r.lon, distM: r.dist_m, device: r.device, deleted: r.deleted === 1,
    };
  }

  broadcast(message, except) {
    const text = JSON.stringify(message);
    for (const ws of this.ctx.getWebSockets()) {
      if (ws === except) continue;
      try { ws.send(text); } catch (_) { /* socket closing */ }
    }
  }

  broadcastPresence(leaving) {
    const online = this.ctx.getWebSockets().filter((ws) => ws !== leaving).length;
    this.broadcast({ type: "presence", online }, leaving);
  }

  // Insert or update one record coming from a phone. Returns the stored event (with server seq).
  upsertLog(e) {
    if (!e || typeof e.uuid !== "string" || !e.uuid || typeof e.tag !== "string" || !e.tag) return null;
    const action = e.action === "OPEN" ? "OPEN" : e.action === "CLOSE" ? "CLOSE" : null;
    if (!action) return null;
    const existing = this.sql.exec(`SELECT * FROM valve_log WHERE uuid=?`, e.uuid).toArray()[0];
    const wantDeleted = e.deleted ? 1 : 0;
    if (existing && existing.deleted === wantDeleted) return this.rowToEvent(existing); // duplicate send
    const seq = this.nextSeq();
    if (existing) {
      // only change allowed after creation: delete (tombstone)
      this.sql.exec(`UPDATE valve_log SET deleted=?, seq=?, received=? WHERE uuid=?`, wantDeleted, seq, Date.now(), e.uuid);
    } else {
      this.sql.exec(
        `INSERT INTO valve_log (seq, uuid, ts, tag, action, system, ph, block, area_ha, operator, note, lat, lon, dist_m, device, deleted, received)
         VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)`,
        seq, e.uuid, Number(e.ts) || Date.now(), e.tag, action, e.system ?? null, e.ph ?? null, e.block ?? null,
        e.areaHa ?? null, (e.operator || "Unknown").slice(0, 80), (e.note || "").slice(0, 500),
        e.lat ?? null, e.lon ?? null, e.distM ?? null, (e.device || "").slice(0, 64), wantDeleted, Date.now()
      );
    }
    return this.rowToEvent(this.sql.exec(`SELECT * FROM valve_log WHERE uuid=?`, e.uuid).toArray()[0]);
  }

  setShift(s) {
    const shift = Math.round(Number(s.shift));
    if (!s.tag || !(shift >= 1 && shift <= 12)) return null;
    const seq = this.nextSeq();
    this.sql.exec(
      `INSERT INTO valve_shift (tag, shift, seq, operator, updated) VALUES (?,?,?,?,?)
       ON CONFLICT(tag) DO UPDATE SET shift=excluded.shift, seq=excluded.seq, operator=excluded.operator, updated=excluded.updated`,
      s.tag, shift, seq, (s.operator || "").slice(0, 80), Date.now()
    );
    return { type: "shift", seq, tag: s.tag, shift, operator: s.operator || "" };
  }

  changesSince(since, limit) {
    const logs = this.sql.exec(`SELECT * FROM valve_log WHERE seq > ? ORDER BY seq LIMIT ?`, since, limit).toArray().map((r) => this.rowToEvent(r));
    const shifts = this.sql.exec(`SELECT tag, shift, seq, operator FROM valve_shift WHERE seq > ? ORDER BY seq LIMIT ?`, since, limit)
      .toArray().map((r) => ({ type: "shift", seq: r.seq, tag: r.tag, shift: r.shift, operator: r.operator }));
    const events = logs.concat(shifts).sort((a, b) => a.seq - b.seq).slice(0, limit);
    const row = this.sql.exec(`SELECT v FROM meta WHERE k='seq'`).toArray()[0];
    const latest = row ? row.v : 0;
    const upTo = events.length ? events[events.length - 1].seq : latest;
    return { events, upTo, latest, more: upTo < latest };
  }

  async fetch(request) {
    const url = new URL(request.url);

    if (url.pathname === "/ws") {
      if (request.headers.get("upgrade") !== "websocket") return json({ error: "Expected WebSocket" }, 426);
      const pair = new WebSocketPair();
      this.ctx.acceptWebSocket(pair[1]); // hibernation API: no cost while idle
      const res = new Response(null, { status: 101, webSocket: pair[0] });
      // tell everyone (including the new phone) how many are online
      queueMicrotask(() => this.broadcastPresence());
      return res;
    }

    if (url.pathname === "/logs" && request.method === "GET") {
      const since = Math.max(0, Number(url.searchParams.get("since")) || 0);
      const limit = Math.min(2000, Math.max(1, Number(url.searchParams.get("limit")) || 1000));
      return json(this.changesSince(since, limit));
    }

    if (url.pathname === "/logs" && request.method === "POST") {
      let body;
      try { body = await request.json(); } catch (_) { return json({ error: "Invalid JSON" }, 400); }
      const items = Array.isArray(body) ? body : [body];
      if (items.length > 500) return json({ error: "Max 500 records per request" }, 413);
      const stored = [];
      for (const e of items) {
        const ev = this.upsertLog(e);
        if (ev) stored.push(ev);
      }
      for (const ev of stored) this.broadcast(ev);
      return json({ stored: stored.map((s) => ({ uuid: s.uuid, seq: s.seq })) });
    }

    if (url.pathname === "/shift" && request.method === "POST") {
      let body;
      try { body = await request.json(); } catch (_) { return json({ error: "Invalid JSON" }, 400); }
      const ev = this.setShift(body || {});
      if (!ev) return json({ error: "tag and shift 1-12 required" }, 400);
      this.broadcast(ev);
      return json(ev);
    }

    if (url.pathname === "/stats" && request.method === "GET") {
      const c = this.sql.exec(`SELECT COUNT(*) AS n FROM valve_log WHERE deleted=0`).toArray()[0].n;
      return json({ records: c, online: this.ctx.getWebSockets().length });
    }

    return json({ error: "Not found" }, 404);
  }

  // Phones may also send records over the socket: {"type":"log", ...} or {"type":"shift", ...}; "ping" keeps alive.
  async webSocketMessage(ws, message) {
    let m;
    try { m = JSON.parse(typeof message === "string" ? message : new TextDecoder().decode(message)); } catch (_) { return; }
    if (m.type === "ping") { ws.send(JSON.stringify({ type: "pong", t: Date.now() })); return; }
    if (m.type === "log") {
      const ev = this.upsertLog(m);
      if (ev) { this.broadcast(ev); }
    } else if (m.type === "shift") {
      const ev = this.setShift(m);
      if (ev) this.broadcast(ev);
    }
  }

  async webSocketClose(ws, code) {
    try { ws.close(code, "bye"); } catch (_) {}
    this.broadcastPresence(ws);
  }

  async webSocketError(ws) {
    this.broadcastPresence(ws);
  }
}
