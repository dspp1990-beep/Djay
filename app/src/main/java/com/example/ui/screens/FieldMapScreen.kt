package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.view.MotionEvent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import com.example.util.SyncManager
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.model.FieldValveLogEntity
import com.example.data.model.GeoUtils
import com.example.data.model.KmlDocument
import com.example.data.model.KmlGeometryType
import com.example.data.model.KmlGeoData
import com.example.data.model.KmlParser
import com.example.ui.theme.Layers
import com.example.ui.theme.MyLocation
import com.example.ui.theme.NearMe
import com.example.ui.viewmodel.IrrigationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import org.osmdroid.util.GeoPoint as OGeo

// ---------------------------------------------------------------------------------------------
// Base layers
// ---------------------------------------------------------------------------------------------

/** Esri World Imagery (satellite). Tile URL order is z/y/x. */
private object EsriWorldImagery : OnlineTileSourceBase(
    "EsriWorldImagery", 0, 19, 256, ".jpg",
    arrayOf("https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/"),
    "Imagery © Esri, Maxar, Earthstar Geographics"
) {
    override fun getTileURLString(pMapTileIndex: Long): String =
        baseUrl + MapTileIndex.getZoom(pMapTileIndex) + "/" +
            MapTileIndex.getY(pMapTileIndex) + "/" + MapTileIndex.getX(pMapTileIndex)
}

// ---------------------------------------------------------------------------------------------
// Layout data taken from the bundled merged KML (assets/sardoba_layout_merged.kml)
// ---------------------------------------------------------------------------------------------

private class LayoutData(
    val cadBlocks: List<List<OGeo>>,
    val newBlocks: List<Pair<String, List<OGeo>>>,
    val mains: List<List<OGeo>>,
    val canals: List<List<OGeo>>,
    val pumpHouses: List<Pair<String, OGeo>>
)

private fun buildLayout(doc: KmlDocument): LayoutData {
    fun pts(f: com.example.data.model.KmlFeature) = f.points.map { OGeo(it.latitude, it.longitude) }
    val cad = mutableListOf<List<OGeo>>()
    val nb = mutableListOf<Pair<String, List<OGeo>>>()
    val mains = mutableListOf<List<OGeo>>()
    val canals = mutableListOf<List<OGeo>>()
    val ph = mutableListOf<Pair<String, OGeo>>()
    for (f in doc.features) {
        val folder = f.folderName ?: ""
        when {
            f.geometryType == KmlGeometryType.POLYGON && folder.contains("New blocks") ->
                nb += f.name.removeSuffix(" (check)") to pts(f)
            f.geometryType == KmlGeometryType.POLYGON && (folder.endsWith("Rashido blocks") || folder.endsWith("T.Malik blocks")) &&
                !f.name.startsWith("Inner") -> cad += pts(f)
            f.geometryType == KmlGeometryType.LINE_STRING && folder.contains("PVC pipe 160") -> mains += pts(f)
            f.geometryType == KmlGeometryType.LINE_STRING && folder.contains("Canal Latoc") -> canals += pts(f)
            f.geometryType == KmlGeometryType.POINT && folder.endsWith("pump houses") ->
                ph += f.name to OGeo(f.points[0].latitude, f.points[0].longitude)
        }
    }
    return LayoutData(cad, nb, mains, canals, ph)
}

// Valve status for TODAY, derived only from the real field log
private enum class FieldStatus { NONE, OPEN, CLOSED }

// ---------------------------------------------------------------------------------------------
// Map overlay: blocks, mains, canal, pump houses and the 719 Jain valves (labels when zoomed in)
// ---------------------------------------------------------------------------------------------

private class FieldOverlay(
    private val valves: List<Pair<String, OGeo>>,
    private val onValveTap: (String) -> Unit
) : Overlay() {
    var layout: LayoutData? = null
    var status: Map<String, FieldStatus> = emptyMap()
    var selectedTag: String? = null

    private val p = Point()
    private val path = Path()
    private val cadPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 3f; color = 0xCCFFFFFF.toInt() }
    private val nbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 2.5f; color = 0xFF00E5FF.toInt() }
    private val mainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 3f; color = 0xFFE040FB.toInt() }
    private val canalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 6f; color = 0xFF00FFFF.toInt() }
    private val phPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL; color = 0xFFFF1744.toInt() }
    private val valveFill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val valveRing = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 2f; color = 0xFF000000.toInt() }
    private val selRing = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 5f; color = 0xFFFFFFFF.toInt() }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFF00.toInt(); textSize = 30f; isFakeBoldText = true
        setShadowLayer(4f, 0f, 0f, 0xFF000000.toInt())
    }
    private val blockLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt(); textSize = 38f; isFakeBoldText = true; textAlign = Paint.Align.CENTER
        setShadowLayer(5f, 0f, 0f, 0xFF000000.toInt())
    }

    private fun drawLine(c: Canvas, mv: MapView, pts: List<OGeo>, paint: Paint, close: Boolean) {
        if (pts.size < 2) return
        path.reset()
        pts.forEachIndexed { i, g ->
            mv.projection.toPixels(g, p)
            if (i == 0) path.moveTo(p.x.toFloat(), p.y.toFloat()) else path.lineTo(p.x.toFloat(), p.y.toFloat())
        }
        if (close) path.close()
        c.drawPath(path, paint)
    }

    override fun draw(c: Canvas, osmv: MapView, shadow: Boolean) {
        if (shadow) return
        val zoom = osmv.zoomLevelDouble
        val dens = osmv.context.resources.displayMetrics.density / 2.75f
        layout?.let { l ->
            l.canals.forEach { drawLine(c, osmv, it, canalPaint, false) }
            l.cadBlocks.forEach { drawLine(c, osmv, it, cadPaint, true) }
            if (zoom >= 13.5) l.newBlocks.forEach { drawLine(c, osmv, it.second, nbPaint, true) }
            if (zoom >= 15.0) l.mains.forEach { drawLine(c, osmv, it, mainPaint, false) }
            if (zoom >= 14.0) {
                blockLabelPaint.textSize = 38f * dens.coerceAtLeast(0.6f)
                l.newBlocks.forEach { (name, pts) ->
                    if (pts.isNotEmpty()) {
                        val lat = pts.map { it.latitude }.average(); val lon = pts.map { it.longitude }.average()
                        osmv.projection.toPixels(OGeo(lat, lon), p)
                        c.drawText(name, p.x.toFloat(), p.y.toFloat(), blockLabelPaint)
                    }
                }
            }
            l.pumpHouses.forEach { (name, g) ->
                osmv.projection.toPixels(g, p)
                c.drawRect(p.x - 12f, p.y - 12f, p.x + 12f, p.y + 12f, phPaint)
                c.drawText(name, p.x + 16f, p.y - 14f, labelPaint)
            }
        }
        if (zoom < 14.0) return  // valves only once zoomed in a bit
        val r = when { zoom >= 18 -> 13f; zoom >= 16.5 -> 10f; else -> 6f }
        labelPaint.textSize = 30f * dens.coerceAtLeast(0.6f)
        val showLabels = zoom >= 16.5
        for ((tag, g) in valves) {
            osmv.projection.toPixels(g, p)
            if (p.x < -50 || p.y < -50 || p.x > c.width + 50 || p.y > c.height + 50) continue
            valveFill.color = when (status[tag]) {
                FieldStatus.OPEN -> 0xFF00E676.toInt()
                FieldStatus.CLOSED -> 0xFFFF5252.toInt()
                else -> 0xFFFFEB3B.toInt()
            }
            c.drawCircle(p.x.toFloat(), p.y.toFloat(), r, valveFill)
            c.drawCircle(p.x.toFloat(), p.y.toFloat(), r, valveRing)
            if (tag == selectedTag) c.drawCircle(p.x.toFloat(), p.y.toFloat(), r + 8f, selRing)
            if (showLabels) c.drawText(tag, p.x + r + 4f, p.y + 10f, labelPaint)
        }
    }

    override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
        if (mapView.zoomLevelDouble < 14.0) return false
        var best: String? = null
        var bestD = 48f * 48f
        for ((tag, g) in valves) {
            mapView.projection.toPixels(g, p)
            val dx = p.x - e.x; val dy = p.y - e.y
            val d = dx * dx + dy * dy
            if (d < bestD) { bestD = d; best = tag }
        }
        return if (best != null) { onValveTap(best); true } else false
    }
}

// ---------------------------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------------------------

private fun startOfToday(): Long = Calendar.getInstance().apply {
    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
}.timeInMillis

private fun logsToCsv(logs: List<FieldValveLogEntity>): String {
    val d = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val t = SimpleDateFormat("HH:mm", Locale.US)
    val sb = StringBuilder("date,time,valve,action,pump_house,system,new_block,area_ha,operator,note,user_lat,user_lon,distance_to_valve_m\n")
    logs.sortedBy { it.timestamp }.forEach {
        val note = it.note.replace("\"", "'")
        sb.append("${d.format(Date(it.timestamp))},${t.format(Date(it.timestamp))},${it.valveTag},${it.action},${it.pumpHouseId},P${it.systemNumber},${it.newBlock},")
        sb.append(String.format(Locale.US, "%.2f", it.areaHa)).append(",\"${it.operatorName}\",\"$note\",")
        sb.append("${it.userLatitude ?: ""},${it.userLongitude ?: ""},${it.distanceToValveM?.let { v -> String.format(Locale.US, "%.0f", v) } ?: ""}\n")
    }
    return sb.toString()
}

private fun shareText(context: Context, subject: String, text: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(send, "Share valve log"))
}

// ---------------------------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FieldMapScreen(viewModel: IrrigationViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val prefs = remember { context.getSharedPreferences("field_app", Context.MODE_PRIVATE) }

    val fieldLogs by viewModel.fieldLogs.collectAsState()
    val allValves by viewModel.allValves.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val unsynced by viewModel.unsyncedCount.collectAsState()
    var showSyncSettings by remember { mutableStateOf(false) }

    var operatorName by remember { mutableStateOf(prefs.getString("operator", "") ?: "") }
    var search by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var selectedTag by remember { mutableStateOf<String?>(null) }
    var showLog by remember { mutableStateOf(false) }
    var satellite by remember { mutableStateOf(true) }

    val valves = remember {
        KmlGeoData.SURVEYED_VALVE_COORDINATES.entries
            .sortedWith(compareBy({ KmlGeoData.systemOfTag(it.key) }, { it.key.substringAfter("-").toIntOrNull() ?: 0 }))
            .map { it.key to OGeo(it.value.latitude, it.value.longitude) }
    }

    // osmdroid needs a user agent and an app-private cache folder
    val mapView = remember {
        Configuration.getInstance().apply {
            load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
            userAgentValue = context.packageName
            osmdroidBasePath = File(context.cacheDir, "osmdroid")
            osmdroidTileCache = File(context.cacheDir, "osmdroid/tiles")
        }
        MapView(context).apply {
            setTileSource(EsriWorldImagery)
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            controller.setZoom(13.5)
            controller.setCenter(OGeo(40.4215, 68.3260))
        }
    }
    val overlay = remember { FieldOverlay(valves) { tag -> selectedTag = tag } }
    val myLocation = remember { MyLocationNewOverlay(GpsMyLocationProvider(context), mapView) }

    LaunchedEffect(Unit) {
        mapView.overlays.add(overlay)
        mapView.overlays.add(myLocation)
        mapView.overlays.add(CopyrightOverlay(context))
    }

    // Load the layout from the bundled merged KML
    val layout by produceState<LayoutData?>(initialValue = null) {
        value = withContext(Dispatchers.IO) {
            try {
                val text = context.assets.open("sardoba_layout_merged.kml").bufferedReader().use { it.readText() }
                buildLayout(KmlParser.parseKmlText(text, "sardoba_layout_merged.kml"))
            } catch (e: Exception) { null }
        }
    }
    LaunchedEffect(layout) { overlay.layout = layout; mapView.invalidate() }

    // Today's status per valve from the REAL field log (latest action wins)
    val todayStart = startOfToday()
    val todayLogs = fieldLogs.filter { it.timestamp >= todayStart }
    val statusMap = remember(fieldLogs) {
        todayLogs.sortedBy { it.timestamp }.associate { it.valveTag to if (it.action == "OPEN") FieldStatus.OPEN else FieldStatus.CLOSED }
    }
    LaunchedEffect(statusMap, selectedTag) {
        overlay.status = statusMap; overlay.selectedTag = selectedTag; mapView.invalidate()
    }
    LaunchedEffect(satellite) {
        mapView.setTileSource(if (satellite) EsriWorldImagery else TileSourceFactory.MAPNIK)
    }

    // Location permission -> real GPS
    fun hasLocation() = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { res ->
        val fine = res[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarse = res[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fine || coarse) {
            myLocation.enableMyLocation()
            viewModel.enableRealGps(fine, coarse)
        } else message = "Location permission denied - GPS features are off"
    }
    LaunchedEffect(Unit) {
        if (hasLocation()) { myLocation.enableMyLocation(); viewModel.enableRealGps(true, true) }
        else permLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    DisposableEffect(lifecycleOwner) {
        val obs = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        mapView.onResume()
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(obs)
            myLocation.disableMyLocation()
            mapView.onPause()
            mapView.onDetach()
        }
    }

    fun goTo(tag: String, zoom: Double = 18.0) {
        val g = KmlGeoData.SURVEYED_VALVE_COORDINATES[tag] ?: return
        mapView.controller.setZoom(zoom)
        mapView.controller.animateTo(OGeo(g.latitude, g.longitude))
        selectedTag = tag
    }

    fun doSearch() {
        val q = search.trim().uppercase(Locale.US).replace(" ", "")
        if (q.isEmpty()) return
        val tag = if (q.startsWith("P")) q else "P$q"
        if (KmlGeoData.SURVEYED_VALVE_COORDINATES.containsKey(tag)) { goTo(tag); message = null; return }
        // new block name, e.g. PH12-1
        val block = layout?.newBlocks?.firstOrNull { it.first.equals(q, ignoreCase = true) || it.first.replace("-", "").equals(q.replace("-", ""), ignoreCase = true) }
        if (block != null) {
            val lat = block.second.map { it.latitude }.average(); val lon = block.second.map { it.longitude }.average()
            mapView.controller.setZoom(16.5); mapView.controller.animateTo(OGeo(lat, lon)); message = null
        } else message = "Not found: $q  (try P29-3 or PH12-1)"
    }

    fun nearestValve() {
        val me = myLocation.myLocation
        if (me == null) { message = if (hasLocation()) "Waiting for GPS fix..." else "Allow location to find the nearest valve"; return }
        val best = valves.minByOrNull { (_, g) -> GeoUtils.distanceMeters(me.latitude, me.longitude, g.latitude, g.longitude) } ?: return
        val d = GeoUtils.distanceMeters(me.latitude, me.longitude, best.second.latitude, best.second.longitude)
        val dir = GeoUtils.bearingToCardinal(me.latitude, me.longitude, best.second.latitude, best.second.longitude)
        goTo(best.first)
        message = "Nearest valve ${best.first}: ${d.toInt()} m $dir"
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())

        // ---- top: search ----
        Column(Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(8.dp)) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 6.dp)) {
                    OutlinedTextField(
                        value = search,
                        onValueChange = { search = it },
                        placeholder = { Text("Valve (P29-3) or block (PH12-1)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { doSearch() }),
                        modifier = Modifier.weight(1f).padding(vertical = 4.dp)
                    )
                    IconButton(onClick = { doSearch() }) { Icon(Icons.Default.Search, contentDescription = "Search") }
                }
            }
            Spacer(Modifier.height(6.dp))
            SyncChip(syncStatus, unsynced) { showSyncSettings = true }
            message?.let {
                Spacer(Modifier.height(6.dp))
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 12.dp)) {
                        Text(it, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                        IconButton(onClick = { message = null }) { Icon(Icons.Default.Close, contentDescription = "Dismiss") }
                    }
                }
            }
        }

        // ---- right: map buttons ----
        Column(
            Modifier.align(Alignment.CenterEnd).padding(end = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SmallFloatingActionButton(onClick = { satellite = !satellite }) { Icon(Icons.Filled.Layers, contentDescription = "Satellite / map") }
            SmallFloatingActionButton(onClick = {
                val me = myLocation.myLocation
                if (me != null) { mapView.controller.setZoom(17.0); mapView.controller.animateTo(me) }
                else message = if (hasLocation()) "Waiting for GPS fix..." else "Allow location in phone settings"
            }) { Icon(Icons.Filled.MyLocation, contentDescription = "My location") }
            SmallFloatingActionButton(onClick = { nearestValve() }) { Icon(Icons.Filled.NearMe, contentDescription = "Nearest valve") }
        }

        // ---- bottom: today summary ----
        val openedToday = todayLogs.filter { it.action == "OPEN" }
        val uniqueOpened = openedToday.map { it.valveTag }.toSet()
        val areaToday = uniqueOpened.sumOf { KmlGeoData.valveAreaHa(it) }
        val openNow = statusMap.count { it.value == FieldStatus.OPEN }
        Card(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
        ) {
            Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Today", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "${uniqueOpened.size} valves opened · ${String.format(Locale.US, "%.1f", areaToday)} ha · $openNow open now",
                        style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold
                    )
                    Text("Yellow = not logged today · Green = open · Red = closed", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(onClick = { showLog = true }) { Text("Log") }
                IconButton(onClick = {
                    if (todayLogs.isEmpty()) message = "Nothing logged today"
                    else shareText(context, "Valve log ${SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())}", logsToCsv(todayLogs))
                }) { Icon(Icons.Default.Share, contentDescription = "Share today's log") }
            }
        }
    }

    // ---- valve sheet ----
    selectedTag?.let { tag ->
        val valve = allValves.firstOrNull { it.tag == tag }
        val last = fieldLogs.firstOrNull { it.valveTag == tag }  // newest first
        val me = myLocation.myLocation
        val pos = KmlGeoData.SURVEYED_VALVE_COORDINATES[tag]
        var note by remember(tag) { mutableStateOf("") }
        ModalBottomSheet(onDismissRequest = { selectedTag = null }) {
            Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
                Text(tag, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(
                    "${KmlGeoData.pumpHouseOfTag(tag)} · System ${KmlGeoData.systemOfTag(tag)} · Block ${KmlGeoData.VALVE_NEW_BLOCK[tag] ?: "-"} · " +
                        String.format(Locale.US, "%.2f ha", KmlGeoData.valveAreaHa(tag)),
                    style = MaterialTheme.typography.bodyMedium
                )
                if (me != null && pos != null) {
                    val d = GeoUtils.distanceMeters(me.latitude, me.longitude, pos.latitude, pos.longitude)
                    Text("${d.toInt()} m ${GeoUtils.bearingToCardinal(me.latitude, me.longitude, pos.latitude, pos.longitude)} of you",
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                }
                Text(
                    last?.let { "Last: ${it.action} by ${it.operatorName}, ${SimpleDateFormat("dd MMM HH:mm", Locale.US).format(Date(it.timestamp))}" }
                        ?: "No field record yet",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = operatorName,
                    onValueChange = {
                        operatorName = it
                        prefs.edit().putString("operator", it).apply()
                        context.getSharedPreferences("sync", Context.MODE_PRIVATE).edit().putString("operator", it).apply()
                    },
                    label = { Text("Your name") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Note (optional)") },
                    singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            viewModel.logFieldValve(tag, true, operatorName, note, me?.latitude, me?.longitude)
                            message = "$tag logged OPEN"; selectedTag = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        modifier = Modifier.weight(1f).height(52.dp)
                    ) { Text("OPEN", fontWeight = FontWeight.Bold) }
                    Button(
                        onClick = {
                            viewModel.logFieldValve(tag, false, operatorName, note, me?.latitude, me?.longitude)
                            message = "$tag logged CLOSED"; selectedTag = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                        modifier = Modifier.weight(1f).height(52.dp)
                    ) { Text("CLOSE", fontWeight = FontWeight.Bold) }
                }
                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                // Shift planner: shift number of this valve (used by the Valves and Schedule screens)
                val shift = valve?.shiftNumber ?: 0
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Shift", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                    OutlinedButton(onClick = { if (shift > 1) viewModel.setValveShift(tag, shift - 1) }, enabled = valve != null) { Text("-") }
                    Text(if (valve != null) "$shift / 12" else "-", modifier = Modifier.padding(horizontal = 14.dp), fontWeight = FontWeight.Bold)
                    OutlinedButton(onClick = { if (shift < 12) viewModel.setValveShift(tag, shift + 1) }, enabled = valve != null) { Text("+") }
                }
            }
        }
    }

    // ---- sync settings ----
    if (showSyncSettings) {
        var url by remember { mutableStateOf(viewModel.syncManager.serverUrl()) }
        var key by remember { mutableStateOf(viewModel.syncManager.teamKey()) }
        AlertDialog(
            onDismissRequest = { showSyncSettings = false },
            title = { Text("Live sync") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        when (syncStatus.state) {
                            SyncManager.State.ONLINE -> "Connected - ${syncStatus.onlineUsers} phone(s) online now."
                            SyncManager.State.CONNECTING -> "Connecting..."
                            SyncManager.State.OFFLINE -> "No connection. Records are kept on this phone and sent automatically when signal returns."
                            SyncManager.State.AUTH_FAILED -> "The team key is wrong."
                            SyncManager.State.NOT_CONFIGURED -> "Enter the sync server address and team key (from your admin)."
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (unsynced > 0) Text("$unsynced record(s) waiting to be sent", color = Color(0xFFE65100))
                    OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text("Server address") },
                        placeholder = { Text("https://smart-irrigation-sync.<name>.workers.dev") }, singleLine = true)
                    OutlinedTextField(value = key, onValueChange = { key = it }, label = { Text("Team key") }, singleLine = true)
                    Text("Device: ${viewModel.syncManager.deviceId.take(8)}", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.configureSync(url, key); showSyncSettings = false }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.syncManager.requestSync(); showSyncSettings = false }) { Text("Sync now") }
            }
        )
    }

    // ---- log sheet ----
    if (showLog) {
        var onlyToday by remember { mutableStateOf(true) }
        val shown = if (onlyToday) todayLogs else fieldLogs
        ModalBottomSheet(onDismissRequest = { showLog = false }) {
            Column(Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Field valve log", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    IconButton(onClick = {
                        if (shown.isEmpty()) message = "Nothing to share"
                        else shareText(context, if (onlyToday) "Valve log today" else "Valve log (all)", logsToCsv(shown))
                    }) { Icon(Icons.Default.Share, contentDescription = "Share as CSV") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = onlyToday, onClick = { onlyToday = true }, label = { Text("Today (${todayLogs.size})") })
                    FilterChip(selected = !onlyToday, onClick = { onlyToday = false }, label = { Text("All (${fieldLogs.size})") })
                }
                Spacer(Modifier.height(8.dp))
                if (shown.isEmpty()) {
                    Text("No records. Tap a valve on the map, then OPEN or CLOSE.", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 24.dp))
                } else {
                    val fmt = SimpleDateFormat("dd MMM HH:mm", Locale.US)
                    LazyColumn(Modifier.heightIn(max = 480.dp)) {
                        items(shown, key = { it.uuid }) { log ->
                            Row(
                                Modifier.fillMaxWidth().clickable { showLog = false; goTo(log.valveTag) }.padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    Modifier.size(10.dp).background(
                                        if (log.action == "OPEN") Color(0xFF00C853) else Color(0xFFFF5252), RoundedCornerShape(5.dp)
                                    )
                                )
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        "${log.valveTag}  ${log.action}" + if (!log.synced) "   · not sent yet" else "",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        "${fmt.format(Date(log.timestamp))} · ${log.operatorName} · ${log.pumpHouseId} · ${log.newBlock}" +
                                            (if (log.note.isNotBlank()) " · ${log.note}" else ""),
                                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { viewModel.deleteFieldLog(log.uuid) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete record")
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SyncChip(status: SyncManager.Status, unsynced: Int, onClick: () -> Unit) {
    val (dot, text) = when (status.state) {
        SyncManager.State.ONLINE -> Color(0xFF00C853) to
            ("Live · ${status.onlineUsers} online" + if (unsynced > 0) " · sending $unsynced" else "")
        SyncManager.State.CONNECTING -> Color(0xFFFFC107) to "Connecting..." + if (unsynced > 0) " · $unsynced waiting" else ""
        SyncManager.State.OFFLINE -> Color(0xFFFF9800) to "Offline · " + if (unsynced > 0) "$unsynced waiting to send" else "saved on phone"
        SyncManager.State.AUTH_FAILED -> Color(0xFFFF1744) to "Sync: wrong team key - tap to fix"
        SyncManager.State.NOT_CONFIGURED -> Color(0xFF9E9E9E) to "Sync not set up - tap"
    }
    Row(
        Modifier
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(10.dp).background(dot, RoundedCornerShape(5.dp)))
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}
