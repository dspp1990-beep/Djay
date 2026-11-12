# GIS data changes (Sardoba merged layout)

## Source files
- `Sardoba Rasido - final.kml`: the base CAD export for PH 12–18 (Sh. Rashido). 21 blocks, 1,099.19 ha; the drawing states 1,099.16 ha.
- `Sardoba Rasido - combined PH 12 TO 18 (2).kml`: has the same geometry as the base file (396/396 objects), so it was not merged twice.
- `Combine PH-7 to 11 (2).kml`: CAD export for T. Malik. 8 blocks, 649.49 ha; the drawing states 649.50 ha.
- `Jain Design MAP Sardoba.pdf`: vector drawing, page 1 = Sh. Rashido, page 2 = T. Malik. It was georeferenced with an affine fit to the CAD pipe lines (median error ~1 m, 95th percentile under 3.5 m).

## What changed in the app
1. **`app/src/main/assets/sardoba_layout_merged.kml`** (new). The GIS map now opens this file by default instead of the demo sample. It contains:
   - CAD blocks
   - pump houses
   - CAD line layers
   - from the Jain drawing: elevation contours, Latoc canal, collectors, drain collectors, 160 mm mains, valve symbols and drawing text.
2. **`IrrigationViewModel.loadBundledLayoutKml()`** loads that asset. It falls back to `createSampleKml()` if the asset is missing. A KML already cached on the device still takes priority; tap "load sample" or clear app data to see the new one.
3. **`KmlParser` bug fix.** A Placemark's `<name>` used to overwrite the folder name, so every feature became its own folder and the feature names were lost (379 folders from the merged file). After the fix: 35 named folders, with all feature names intact.
4. **`BlueprintStructures.SURVEYED_PLOTS`**
   - 27 plots now use the real CAD block outlines, matched by area to within 0.2%. All matches were exact to 3 decimals.
   - 2 CAD blocks with no counterpart were added as `plot_cad_*` with `UNASSIGNED` PH.
   - `pumpHouseId`/`systemNumber` were left as before and flagged in comments as unverified.
5. **`BlueprintStructures.ELEVATION_CONTOURS`**: the invented lines were replaced by 36 labelled contours (278.5–283.0 m) taken from the Jain drawing.
6. **`KmlGeoData.PUMP_HOUSE_LOCATIONS["PH-16"]`**: corrected from the guess (40.4215, 68.3005) to the CAD point 40.4196591, 68.2969086. This is about 330 m from the old guess.

## Valves (added 12 Nov 2026)
- **`KmlGeoData.SURVEYED_VALVE_COORDINATES`** now holds all 719 valves: 456 at Sh. Rashido and 263 at T. Malik.
  - Each position is a valve symbol from the Jain drawing, matched to its printed tag `P<system>-<n>`. These are the same tags `SYSTEM_SPECS` and `SeedData` already use.
  - Every tag in `SYSTEM_SPECS` now resolves to a real position, so the app never falls back to interpolating along the pipeline.
  - The data is packed into a single string because a large `mapOf()` can exceed the JVM's 64 KB method-size limit.
- **New lookup `VALVE_NEW_BLOCK`** (tag → new block from "Drip New block 2026 Nov.xlsx").
- **The asset KML now includes the 54 new blocks** (auto-split, to review) and the 719 valves, each named by its Jain tag. Valves can be searched by name, e.g. "P29-3".
- **Valve numbers are the Jain drawing tags** (`P<system>-<n>`). Jain numbers each pump house column by column, so one new block usually holds several runs of numbers.
- **`SURVEYED_FLUSH_VALVE_COORDINATES` is still the old hand-entered list.** The drawing's flush-valve symbols were not extracted.

## Still open (needs the person who knows the drawing)
- **Which blocks each system / pump house feeds.** The drawing gives PH→system (PH-15→32/33/34, PH-16→35/36/37, PH-17→38/39/40, PH-18→41/42/43, PH-7..11→24..28), but not system→block. Tracing the CAD pipe network gives clear answers for PH-12, 14, 16, 18 and PH-7..11. It is ambiguous for PH-13/15 and does not reach the 10 southern Rashido blocks.
- **Canal / road vectors in `BlueprintStructures.CANALS/ROADS`** are still the old approximations. The map does not use them; the real ones are in the asset KML.
- **New-block outlines are an auto-split** of the CAD blocks along valve rows. 12 of the 54 blocks have a green/gross ratio outside 0.80–1.05 and are marked "check".
- **Excel naming vs drawing:** Excel "PH16-13..18" are fed by PH-17, and Excel "PH7-x" covers PH-7..11. Valve numbers follow the physical pump house.
