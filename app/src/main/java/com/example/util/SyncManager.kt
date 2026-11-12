package com.example.util

import android.content.Context
import com.example.BuildConfig
import com.example.data.local.IrrigationDao
import com.example.data.model.FieldValveLogEntity
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Live multi-user sync with the Cloudflare sync server (cloud/ folder).
 *
 * Offline-first: every OPEN/CLOSE is written to the phone's database first (synced = false) and sent
 * when there is signal. While online, a WebSocket delivers other users' records instantly; after every
 * (re)connect the phone also pulls everything it missed (GET /logs?since=lastSeq).
 */
class SyncManager(
    context: Context,
    private val dao: IrrigationDao,
    private val scope: CoroutineScope
) {
    enum class State { NOT_CONFIGURED, CONNECTING, ONLINE, OFFLINE, AUTH_FAILED }

    private class CloseInfo(val httpCode: Int?)

    data class Status(
        val state: State,
        val onlineUsers: Int = 0,
        val lastSyncAt: Long = 0L,
        val message: String? = null
    )

    private val prefs = context.applicationContext.getSharedPreferences("sync", Context.MODE_PRIVATE)
    private val jsonType = "application/json; charset=utf-8".toMediaType()
    private val http = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    private val wsClient = http.newBuilder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .pingInterval(25, TimeUnit.SECONDS)
        .build()

    private val _status = MutableStateFlow(Status(State.NOT_CONFIGURED))
    val status: StateFlow<Status> = _status.asStateFlow()

    val deviceId: String = prefs.getString("device_id", null) ?: UUID.randomUUID().toString().also {
        prefs.edit().putString("device_id", it).apply()
    }

    private val syncLock = Mutex()
    private var loopJob: Job? = null
    private var socket: WebSocket? = null
    @Volatile private var configChanged = false

    // ------------------------------------------------------------------ settings
    fun serverUrl(): String =
        (prefs.getString("url", null)?.takeIf { it.isNotBlank() } ?: BuildConfig.SYNC_URL).trim().trimEnd('/')

    fun teamKey(): String =
        (prefs.getString("key", null)?.takeIf { it.isNotBlank() } ?: BuildConfig.SYNC_TEAM_KEY).trim()

    fun isConfigured(): Boolean = serverUrl().startsWith("http") && teamKey().isNotEmpty()

    fun configure(url: String, key: String) {
        prefs.edit().putString("url", url.trim()).putString("key", key.trim()).putLong("last_seq", 0L).apply()
        configChanged = true
        socket?.close(1000, "reconfigured")
    }

    private var lastSeq: Long
        get() = prefs.getLong("last_seq", 0L)
        set(v) { prefs.edit().putLong("last_seq", v).apply() }

    // ------------------------------------------------------------------ lifecycle
    fun start() {
        if (loopJob?.isActive == true) return
        loopJob = scope.launch(Dispatchers.IO) { connectionLoop() }
    }

    fun stop() {
        loopJob?.cancel()
        socket?.close(1000, "app closed")
        socket = null
    }

    /** Call after any local change: sends immediately if online, otherwise it waits in the queue. */
    fun requestSync() {
        if (!isConfigured()) return
        scope.launch(Dispatchers.IO) { runCatching { syncNow() } }
    }

    fun queueShift(tag: String, shift: Int) {
        val pending = JSONObject(prefs.getString("pending_shifts", "{}") ?: "{}")
        pending.put(tag, shift)
        prefs.edit().putString("pending_shifts", pending.toString()).apply()
        requestSync()
    }

    private suspend fun connectionLoop() {
        var backoffMs = 3_000L
        while (scope.isActive) {
            if (!isConfigured()) {
                _status.value = Status(State.NOT_CONFIGURED, message = "Enter server address and team key")
                delay(3_000); continue
            }
            configChanged = false
            _status.value = _status.value.copy(state = State.CONNECTING, message = null)
            val closed = CompletableDeferred<CloseInfo>()   // httpCode 401 = wrong key
            val wsUrl = serverUrl().replaceFirst(Regex("^http"), "ws") + "/ws?key=" + URLEncoder.encode(teamKey(), "UTF-8")
            socket = wsClient.newWebSocket(Request.Builder().url(wsUrl).build(), object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    _status.value = _status.value.copy(state = State.ONLINE, message = null)
                    requestSync()
                }
                override fun onMessage(webSocket: WebSocket, text: String) {
                    scope.launch(Dispatchers.IO) { runCatching { handleServerMessage(JSONObject(text)) } }
                }
                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) { closed.complete(CloseInfo(null)) }
                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    closed.complete(CloseInfo(response?.code))
                }
            })
            // While connected: safety-net catch-up every 60 s
            var code: Int? = null
            var wasOnline = false
            while (true) {
                val done: CloseInfo? = withTimeoutOrNull(60_000) { closed.await() }
                if (_status.value.state == State.ONLINE) wasOnline = true
                if (done != null) { code = done.httpCode; break }
                runCatching { syncNow() }
            }
            socket = null
            if (code == 401) {
                _status.value = Status(State.AUTH_FAILED, message = "Wrong team key")
                while (scope.isActive && !configChanged) delay(2_000)
                continue
            }
            _status.value = _status.value.copy(state = State.OFFLINE, onlineUsers = 0)
            if (wasOnline) backoffMs = 3_000L
            if (!configChanged) delay(backoffMs)
            backoffMs = (backoffMs * 2).coerceAtMost(60_000L)
        }
    }

    // ------------------------------------------------------------------ sync
    /** Push queued local records + shift changes, then pull everything newer than lastSeq. */
    suspend fun syncNow() = syncLock.withLock {
        if (!isConfigured()) return@withLock
        // 1) push unsent valve records (incl. deletes)
        val pending = dao.getUnsyncedFieldLogs()
        pending.chunked(200).forEach { chunk ->
            val arr = JSONArray().apply { chunk.forEach { put(toJson(it)) } }
            val res = post("/logs", arr.toString()) ?: return@withLock
            val stored = res.optJSONArray("stored") ?: JSONArray()
            for (i in 0 until stored.length()) {
                val s = stored.getJSONObject(i)
                dao.markFieldLogSynced(s.getString("uuid"), s.getLong("seq"))
            }
        }
        // 2) push queued shift changes
        val shifts = JSONObject(prefs.getString("pending_shifts", "{}") ?: "{}")
        val sent = mutableListOf<String>()
        shifts.keys().forEach { tag ->
            val body = JSONObject().put("tag", tag).put("shift", shifts.getInt(tag)).put("operator", prefs.getString("operator", "") ?: "")
            if (post("/shift", body.toString()) != null) sent += tag
        }
        if (sent.isNotEmpty()) {
            val left = JSONObject(prefs.getString("pending_shifts", "{}") ?: "{}")
            sent.forEach { left.remove(it) }
            prefs.edit().putString("pending_shifts", left.toString()).apply()
        }
        // 3) pull what this phone missed
        var guard = 0
        while (guard++ < 50) {
            val page = get("/logs?since=$lastSeq&limit=1000") ?: return@withLock
            val events = page.optJSONArray("events") ?: JSONArray()
            for (i in 0 until events.length()) handleServerMessage(events.getJSONObject(i))
            lastSeq = page.optLong("upTo", lastSeq)
            if (!page.optBoolean("more", false)) break
        }
        _status.value = _status.value.copy(lastSyncAt = System.currentTimeMillis())
    }

    private suspend fun handleServerMessage(m: JSONObject) {
        when (m.optString("type")) {
            "log" -> {
                val incoming = fromJson(m)
                val local = dao.getFieldLog(incoming.uuid)
                // keep a delete made on this phone that the server hasn't received yet
                if (local != null && local.deleted && !local.synced && !incoming.deleted) return
                dao.upsertFieldLog(incoming)
            }
            "shift" -> {
                val tag = m.optString("tag")
                val shift = m.optInt("shift", 0)
                if (tag.isNotEmpty() && shift in 1..12) dao.setValveShift(tag, shift)
            }
            "presence" -> _status.value = _status.value.copy(onlineUsers = m.optInt("online", 0))
        }
    }

    // ------------------------------------------------------------------ HTTP helpers (run on IO)
    private fun post(path: String, body: String): JSONObject? = runCatching {
        val req = Request.Builder().url(serverUrl() + path).header("X-Team-Key", teamKey())
            .post(body.toRequestBody(jsonType)).build()
        http.newCall(req).execute().use { r ->
            if (r.code == 401) _status.value = Status(State.AUTH_FAILED, message = "Wrong team key")
            if (!r.isSuccessful) null else JSONObject(r.body?.string() ?: "{}")
        }
    }.getOrNull()

    private fun get(path: String): JSONObject? = runCatching {
        val req = Request.Builder().url(serverUrl() + path).header("X-Team-Key", teamKey()).get().build()
        http.newCall(req).execute().use { r ->
            if (r.code == 401) _status.value = Status(State.AUTH_FAILED, message = "Wrong team key")
            if (!r.isSuccessful) null else JSONObject(r.body?.string() ?: "{}")
        }
    }.getOrNull()

    // ------------------------------------------------------------------ JSON mapping
    private fun toJson(e: FieldValveLogEntity) = JSONObject().apply {
        put("uuid", e.uuid); put("ts", e.timestamp); put("tag", e.valveTag); put("action", e.action)
        put("system", e.systemNumber); put("ph", e.pumpHouseId); put("block", e.newBlock); put("areaHa", e.areaHa)
        put("operator", e.operatorName); put("note", e.note); put("device", e.deviceId); put("deleted", e.deleted)
        e.userLatitude?.let { put("lat", it) }; e.userLongitude?.let { put("lon", it) }; e.distanceToValveM?.let { put("distM", it) }
    }

    private fun JSONObject.optDoubleOrNull(k: String): Double? = if (!has(k) || isNull(k)) null else optDouble(k)

    private fun fromJson(m: JSONObject) = FieldValveLogEntity(
        uuid = m.getString("uuid"),
        timestamp = m.optLong("ts", System.currentTimeMillis()),
        valveTag = m.optString("tag"),
        systemNumber = if (m.isNull("system")) 0 else m.optInt("system", 0),
        pumpHouseId = if (m.isNull("ph")) "" else m.optString("ph"),
        newBlock = if (m.isNull("block")) "" else m.optString("block"),
        action = m.optString("action"),
        operatorName = if (m.isNull("operator")) "Unknown" else m.optString("operator"),
        note = if (m.isNull("note")) "" else m.optString("note"),
        areaHa = m.optDoubleOrNull("areaHa") ?: 0.0,
        userLatitude = m.optDoubleOrNull("lat"),
        userLongitude = m.optDoubleOrNull("lon"),
        distanceToValveM = m.optDoubleOrNull("distM"),
        deviceId = if (m.isNull("device")) "" else m.optString("device"),
        synced = true,
        serverSeq = m.optLong("seq"),
        deleted = m.optBoolean("deleted", false)
    )
}
