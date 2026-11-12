# Smart Irrigation – Sardoba drip (Sh. Rashido PH 12–18, T. Malik PH 7–11)

An Android app (Kotlin + Jetpack Compose). It started as a Google AI Studio export; the field map, the real layout data and the field valve log were added afterwards.

## What is real and what is demo
| Tab | Data |
|---|---|
| **Field** (opens first) | **Real.** Satellite map (Esri World Imagery, OSM as fallback) showing the CAD blocks, the 54 new blocks (Nov 2026), the 160 mm mains, the Latoc canal, the pump houses and all **719 valves at their Jain-drawing positions, labelled with their Jain tags** (P29-3 …). The app has GPS, nearest-valve, and search by valve or block. Tapping a valve lets you log OPEN or CLOSE with your name and a note. The day summary (valves opened, ha, open now) comes from those records only, and the log can be shared as CSV (e.g. to Telegram). |
| **KML** | Real layout. This is the original AI Studio canvas map loading the merged KML. |
| Overview / Valves / Stations / Sensors / Schedule | **DEMO.** The flows, pressures, sensors, pump status and auto-shift sequence are simulated by `SeedData` and the telemetry loop. A yellow banner marks these screens. |

**Shift planner:** in the Field tab, the valve sheet has a Shift − / + control. It changes the valve's shift (1–12), and the Valves and Schedule screens use that value.

**Field log storage:** the field log is stored **on each phone** (Room table `field_valve_log`). To combine records, use **Share** (CSV). A shared server, such as Firebase or a Google Sheet endpoint, is the next step if all phones should see the same live status.

## Build the APK
### Option A – GitHub (no Android Studio needed)
1. Push this folder to a GitHub repository (branch `main`).
2. `.github/workflows/build-apk.yml` runs automatically. You can also start it by hand from the **Actions** tab with "Run workflow".
3. Open the finished run → **Artifacts** → `smart-irrigation-apk` → unzip → install `app-debug.apk` on the phone. You need to allow "install unknown apps".

### Option B – Android Studio
Open the folder → let Gradle sync → Run. Gradle 9.3.1 / AGP 9.1 / JDK 17+.

## Data sources
- `app/src/main/assets/sardoba_layout_merged.kml`: built from the CAD KMLs "Sardoba Rasido - final" and "Combine PH-7 to 11", plus "Jain Design MAP Sardoba.pdf" (georeferenced), plus "Drip New block 2026 Nov.xlsx".
- `KmlGeoData.SURVEYED_VALVE_COORDINATES`: the 719 valves (Jain tag → position, new block).
- See `GIS_DATA_CHANGES.md` for the details and the open points.

Satellite tiles: Esri World Imagery. Check Esri's terms of use for your organisation's use, or switch to the OSM layer with the layers button.
