package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.*

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GeoPoint
import com.example.data.model.GeoUtils
import com.example.data.model.KmlDocument
import com.example.data.model.KmlFeature
import com.example.data.model.KmlFolder
import com.example.data.model.KmlGeometryType
import com.example.data.model.MeasurePoint
import com.example.ui.viewmodel.IrrigationViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

enum class EarthBasemapTheme {
    SATELLITE_SPACE,
    VECTOR_GIS,
    TERRAIN_TOPO
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GisMapScreen(
    viewModel: IrrigationViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val kmlDocument by viewModel.kmlDocument.collectAsState()
    val selectedFeature by viewModel.selectedKmlFeature.collectAsState()
    val jumpTarget by viewModel.jumpTarget.collectAsState()

    // Map Transformations: Pan & Zoom
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffsetX by remember { mutableFloatStateOf(0.0f) }
    var panOffsetY by remember { mutableFloatStateOf(0.0f) }
    var bearingAngle by remember { mutableFloatStateOf(0.0f) }

    // Display options
    var basemapTheme by remember { mutableStateOf(EarthBasemapTheme.SATELLITE_SPACE) }
    var showGraticuleGrid by remember { mutableStateOf(true) }
    var showFeatureLabels by remember { mutableStateOf(true) }

    // Dialogs & Sheets
    var showPlacesDrawer by remember { mutableStateOf(false) }
    var showPasteXmlDialog by remember { mutableStateOf(false) }
    var pasteXmlInput by remember { mutableStateOf("") }
    var showSearchSheet by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Ruler / Measure Tool Mode
    var isMeasureMode by remember { mutableStateOf(false) }
    val measurePoints = remember { mutableStateListOf<MeasurePoint>() }

    // File Picker for KML / KMZ
    val kmlFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { fileUri ->
            try {
                val inputStream = context.contentResolver.openInputStream(fileUri)
                if (inputStream != null) {
                    val fileName = fileUri.lastPathSegment ?: "Uploaded.kml"
                    viewModel.loadKmlFile(inputStream, fileName)
                    Toast.makeText(context, "KML file imported successfully", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error importing KML: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Function to fit all features on screen
    fun fitBounds() {
        val doc = kmlDocument ?: return
        if (doc.features.isEmpty()) return
        panOffsetX = 0f
        panOffsetY = 0f
        zoomScale = 1.0f
        bearingAngle = 0.0f
    }

    // Function to center camera on a coordinate
    fun centerCameraOn(lat: Double, lng: Double, targetZoom: Float = 3.5f) {
        val doc = kmlDocument ?: return
        val b = doc.bounds
        val relX = ((lng - b.centerLng) / b.spanLng).toFloat()
        val relY = ((b.centerLat - lat) / b.spanLat).toFloat()

        zoomScale = targetZoom
        panOffsetX = -relX * 600f * targetZoom
        panOffsetY = -relY * 600f * targetZoom
    }

    // React to jump targets
    LaunchedEffect(jumpTarget) {
        jumpTarget?.let { pt ->
            centerCameraOn(pt.latitude, pt.longitude, 4.0f)
            viewModel.jumpToTarget(pt) // keep or clear
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B19))
            .testTag("kml_earth_screen")
    ) {
        if (kmlDocument == null || kmlDocument?.features.isNullOrEmpty()) {
            // Empty State: Welcome / Upload Hub
            KmlUploadWelcomeCard(
                onPickFile = { kmlFilePicker.launch("*/*") },
                onPasteXml = { showPasteXmlDialog = true },
                onLoadSample = {
                    viewModel.loadSampleKml()
                    Toast.makeText(context, "Loaded Sample Google Earth Project", Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            val doc = kmlDocument!!

            // Geospatial Canvas Viewport
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val widthPx = constraints.maxWidth.toFloat()
                val heightPx = constraints.maxHeight.toFloat()
                val density = context.resources.displayMetrics.density

                val bounds = doc.bounds
                val centerLat = bounds.centerLat
                val centerLng = bounds.centerLng
                val spanLat = bounds.spanLat
                val spanLng = bounds.spanLng

                // Aspect ratio scaling
                val baseScale = min(widthPx, heightPx) * 0.75f

                fun geoToScreen(lat: Double, lng: Double): Offset {
                    val normX = (lng - centerLng) / spanLng
                    val normY = (centerLat - lat) / spanLat
                    val screenX = (widthPx / 2f) + (normX.toFloat() * baseScale * zoomScale) + panOffsetX
                    val screenY = (heightPx / 2f) + (normY.toFloat() * baseScale * zoomScale) + panOffsetY
                    return Offset(screenX, screenY)
                }

                fun screenToGeo(screenX: Float, screenY: Float): GeoPoint {
                    val normX = (screenX - panOffsetX - (widthPx / 2f)) / (baseScale * zoomScale)
                    val normY = (screenY - panOffsetY - (heightPx / 2f)) / (baseScale * zoomScale)
                    val lng = centerLng + (normX * spanLng)
                    val lat = centerLat - (normY * spanLat)
                    return GeoPoint(lat, lng)
                }

                // Interactive Canvas
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(isMeasureMode) {
                            if (isMeasureMode) {
                                detectTapGestures { tapOffset ->
                                    val geo = screenToGeo(tapOffset.x, tapOffset.y)
                                    measurePoints.add(
                                        MeasurePoint(
                                            id = UUID.randomUUID().toString(),
                                            geoPoint = geo,
                                            label = "Point ${measurePoints.size + 1}"
                                        )
                                    )
                                }
                            } else {
                                detectTapGestures { tapOffset ->
                                    // Hit test features (Point -> Line -> Polygon)
                                    var hitFeature: KmlFeature? = null

                                    // 1. Check Points first (within 32dp)
                                    val pointThreshold = 32f * density
                                    for (feat in doc.features) {
                                        if (!feat.isVisible || (feat.geometryType != KmlGeometryType.POINT && feat.points.size != 1)) continue
                                        if (feat.points.isNotEmpty()) {
                                            val p = geoToScreen(feat.points[0].latitude, feat.points[0].longitude)
                                            val dist = hypot(p.x - tapOffset.x, p.y - tapOffset.y)
                                            if (dist < pointThreshold) {
                                                hitFeature = feat
                                                break
                                            }
                                        }
                                    }

                                    // 2. Check LineStrings (within 24dp)
                                    if (hitFeature == null) {
                                        val lineThreshold = 24f * density
                                        for (feat in doc.features) {
                                            if (!feat.isVisible || (feat.geometryType != KmlGeometryType.LINE_STRING && feat.points.size != 2)) continue
                                            for (i in 0 until feat.points.size - 1) {
                                                val p1 = geoToScreen(feat.points[i].latitude, feat.points[i].longitude)
                                                val p2 = geoToScreen(feat.points[i + 1].latitude, feat.points[i + 1].longitude)
                                                if (distanceToSegment(tapOffset, p1, p2) < lineThreshold) {
                                                    hitFeature = feat
                                                    break
                                                }
                                            }
                                            if (hitFeature != null) break
                                        }
                                    }

                                    // 3. Check Polygons (point-in-polygon)
                                    if (hitFeature == null) {
                                        val tapGeo = screenToGeo(tapOffset.x, tapOffset.y)
                                        for (feat in doc.features) {
                                            if (!feat.isVisible || (feat.geometryType != KmlGeometryType.POLYGON && feat.points.size < 3)) continue
                                            if (isPointInPolygon(tapGeo, feat.points)) {
                                                hitFeature = feat
                                                break
                                            }
                                        }
                                    }

                                    viewModel.selectKmlFeature(hitFeature)
                                }
                            }
                        }
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                zoomScale = (zoomScale * zoom).coerceIn(0.2f, 30.0f)
                                panOffsetX += pan.x
                                panOffsetY += pan.y
                            }
                        }
                ) {
                    // 1. Draw Globe / Satellite Atmospheric Basemap
                    drawEarthBasemap(
                        theme = basemapTheme,
                        width = widthPx,
                        height = heightPx,
                        density = density,
                        panX = panOffsetX,
                        panY = panOffsetY,
                        zoom = zoomScale
                    )

                    // 2. Draw Latitude / Longitude Graticule Grid
                    if (showGraticuleGrid) {
                        drawGraticuleGrid(
                            width = widthPx,
                            height = heightPx,
                            bounds = bounds,
                            zoom = zoomScale,
                            density = density,
                            toScreen = { lat, lng -> geoToScreen(lat, lng) }
                        )
                    }

                    // 3. Render KML Polygons (including MultiGeometry and all boundary plots without restriction)
                    doc.features.filter {
                        it.isVisible && (it.geometryType == KmlGeometryType.POLYGON || (it.geometryType == KmlGeometryType.MULTI_GEOMETRY && it.points.size >= 3) || (it.geometryType !in listOf(KmlGeometryType.POINT, KmlGeometryType.LINE_STRING) && it.points.size >= 3))
                    }.forEach { feat ->
                        val isSelected = selectedFeature?.id == feat.id
                        val path = Path()
                        feat.points.forEachIndexed { idx, pt ->
                            val sPt = geoToScreen(pt.latitude, pt.longitude)
                            if (idx == 0) path.moveTo(sPt.x, sPt.y)
                            else path.lineTo(sPt.x, sPt.y)
                        }
                        path.close()

                        // Fill
                        if (feat.style.isPolyFilled) {
                            val fillColor = if (isSelected) Color(0xFF00E5FF).copy(alpha = 0.45f)
                            else feat.style.polyFillColor
                            drawPath(path, color = fillColor, style = Fill)
                        }

                        // Outline
                        if (feat.style.isPolyOutlined) {
                            val strokeColor = if (isSelected) Color(0xFFFFFFFF) else feat.style.lineColor
                            val strokeWidth = (if (isSelected) feat.style.lineWidth + 2f else feat.style.lineWidth) * density
                            drawPath(
                                path,
                                color = strokeColor,
                                style = Stroke(
                                    width = strokeWidth,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }

                    // 4. Render KML LineStrings / Paths (canals, pipelines, paths without restriction)
                    doc.features.filter {
                        it.isVisible && (it.geometryType == KmlGeometryType.LINE_STRING || (it.geometryType == KmlGeometryType.MULTI_GEOMETRY && it.points.size == 2) || (it.geometryType !in listOf(KmlGeometryType.POINT, KmlGeometryType.POLYGON) && it.points.size == 2))
                    }.forEach { feat ->
                        val isSelected = selectedFeature?.id == feat.id
                        if (feat.points.size >= 2) {
                            val path = Path()
                            feat.points.forEachIndexed { idx, pt ->
                                val sPt = geoToScreen(pt.latitude, pt.longitude)
                                if (idx == 0) path.moveTo(sPt.x, sPt.y)
                                else path.lineTo(sPt.x, sPt.y)
                            }

                            // Glow effect for selected
                            if (isSelected) {
                                drawPath(
                                    path,
                                    color = Color.White.copy(alpha = 0.5f),
                                    style = Stroke(width = (feat.style.lineWidth + 5f) * density, cap = StrokeCap.Round)
                                )
                            }

                            drawPath(
                                path,
                                color = if (isSelected) Color.White else feat.style.lineColor,
                                style = Stroke(
                                    width = (if (isSelected) feat.style.lineWidth + 1.5f else feat.style.lineWidth) * density,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )

                            // Draw vertices
                            feat.points.forEach { pt ->
                                val sPt = geoToScreen(pt.latitude, pt.longitude)
                                drawCircle(
                                    color = if (isSelected) Color.White else feat.style.lineColor,
                                    radius = 2.5f * density,
                                    center = sPt
                                )
                            }
                        }
                    }

                    // 5. Render KML Placemarks / Points (all markers, stations, valves without restriction)
                    doc.features.filter {
                        it.isVisible && (it.geometryType == KmlGeometryType.POINT || it.points.size == 1)
                    }.forEach { feat ->
                        if (feat.points.isNotEmpty()) {
                            val isSelected = selectedFeature?.id == feat.id
                            val pt = feat.points[0]
                            val sPt = geoToScreen(pt.latitude, pt.longitude)

                            val pinColor = if (isSelected) Color(0xFF00E5FF) else feat.style.iconColor
                            val pinRadius = (if (isSelected) 8f else 6.5f) * density

                            // Pin drop shadow
                            drawCircle(
                                color = Color.Black.copy(alpha = 0.45f),
                                radius = pinRadius + 2f * density,
                                center = Offset(sPt.x + 1.5f * density, sPt.y + 2f * density)
                            )

                            // Outer halo
                            drawCircle(
                                color = pinColor.copy(alpha = 0.3f),
                                radius = pinRadius + 5f * density,
                                center = sPt
                            )

                            // Pin head
                            drawCircle(
                                color = Color.White,
                                radius = pinRadius,
                                center = sPt
                            )
                            drawCircle(
                                color = pinColor,
                                radius = pinRadius - 2f * density,
                                center = sPt
                            )

                            // Center dot
                            drawCircle(
                                color = Color.White,
                                radius = 2f * density,
                                center = sPt
                            )
                        }
                    }

                    // 6. Draw Measurement Path (if Ruler is active)
                    if (isMeasureMode && measurePoints.isNotEmpty()) {
                        val mPath = Path()
                        measurePoints.forEachIndexed { idx, mp ->
                            val sPt = geoToScreen(mp.geoPoint.latitude, mp.geoPoint.longitude)
                            if (idx == 0) mPath.moveTo(sPt.x, sPt.y)
                            else mPath.lineTo(sPt.x, sPt.y)
                        }

                        // Dashed measurement line
                        drawPath(
                            mPath,
                            color = Color(0xFFFFD600),
                            style = Stroke(
                                width = 3f * density,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
                            )
                        )

                        // Polygon fill if 3+ points
                        if (measurePoints.size >= 3) {
                            mPath.close()
                            drawPath(
                                mPath,
                                color = Color(0xFFFFD600).copy(alpha = 0.18f),
                                style = Fill
                            )
                        }

                        // Pins
                        measurePoints.forEachIndexed { idx, mp ->
                            val sPt = geoToScreen(mp.geoPoint.latitude, mp.geoPoint.longitude)
                            drawCircle(color = Color.Black, radius = 7f * density, center = sPt)
                            drawCircle(color = Color(0xFFFFD600), radius = 5.5f * density, center = sPt)
                            drawCircle(color = Color.Black, radius = 2f * density, center = sPt)
                        }
                    }
                }

                // --- Top Google Earth Status Bar ---
                GoogleEarthTopBar(
                    doc = doc,
                    onPickNewKml = { kmlFilePicker.launch("*/*") },
                    onPasteXml = { showPasteXmlDialog = true },
                    onOpenSearch = { showSearchSheet = true },
                    onTogglePlaces = { showPlacesDrawer = true },
                    onToggleMeasure = {
                        isMeasureMode = !isMeasureMode
                        if (!isMeasureMode) measurePoints.clear()
                    },
                    isMeasureMode = isMeasureMode,
                    basemapTheme = basemapTheme,
                    onChangeTheme = {
                        basemapTheme = when (basemapTheme) {
                            EarthBasemapTheme.SATELLITE_SPACE -> EarthBasemapTheme.VECTOR_GIS
                            EarthBasemapTheme.VECTOR_GIS -> EarthBasemapTheme.TERRAIN_TOPO
                            EarthBasemapTheme.TERRAIN_TOPO -> EarthBasemapTheme.SATELLITE_SPACE
                        }
                    },
                    modifier = Modifier.align(Alignment.TopCenter)
                )

                // --- Floating Measurement HUD (when Ruler mode active) ---
                if (isMeasureMode) {
                    MeasurementHud(
                        measurePoints = measurePoints,
                        onUndo = { if (measurePoints.isNotEmpty()) measurePoints.removeAt(measurePoints.size - 1) },
                        onClear = { measurePoints.clear() },
                        onClose = {
                            isMeasureMode = false
                            measurePoints.clear()
                        },
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 80.dp)
                    )
                }

                // --- Bottom-Left: Live Geographic Coordinates HUD & Scale Bar ---
                val viewportCenter = screenToGeo(widthPx / 2f, heightPx / 2f)
                CoordinatesScaleHud(
                    center = viewportCenter,
                    zoom = zoomScale,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 14.dp, bottom = 16.dp)
                )

                // --- Floating Navigation & Zoom Controls (Right Column) ---
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // True North Reset
                    FloatingActionButton(
                        onClick = { bearingAngle = 0.0f },
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        contentColor = Color(0xFFEF4444),
                        elevation = FloatingActionButtonDefaults.elevation(3.dp),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            Icons.Default.Explore,
                            contentDescription = "Reset North",
                            modifier = Modifier
                                .size(22.dp)
                                .rotate(bearingAngle)
                        )
                    }

                    // Fit Extents (Zoom to all KML data)
                    FloatingActionButton(
                        onClick = { fitBounds() },
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        contentColor = MaterialTheme.colorScheme.primary,
                        elevation = FloatingActionButtonDefaults.elevation(3.dp),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(Icons.Default.FitScreen, contentDescription = "Fit All Extents", modifier = Modifier.size(20.dp))
                    }

                    // Zoom In (+)
                    FloatingActionButton(
                        onClick = { zoomScale = (zoomScale * 1.35f).coerceAtMost(30.0f) },
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        elevation = FloatingActionButtonDefaults.elevation(3.dp),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Zoom In", modifier = Modifier.size(20.dp))
                    }

                    // Zoom Out (-)
                    FloatingActionButton(
                        onClick = { zoomScale = (zoomScale / 1.35f).coerceAtLeast(0.2f) },
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        elevation = FloatingActionButtonDefaults.elevation(3.dp),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Zoom Out", modifier = Modifier.size(20.dp))
                    }

                    // Places & Layers Panel Toggle
                    FloatingActionButton(
                        onClick = { showPlacesDrawer = true },
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        elevation = FloatingActionButtonDefaults.elevation(4.dp),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(Icons.Default.Layers, contentDescription = "Places Hierarchy", modifier = Modifier.size(20.dp))
                    }
                }

                // --- Google Earth Info Balloon (Popup Card for Selected Feature) ---
                AnimatedVisibility(
                    visible = selectedFeature != null && !isMeasureMode,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    selectedFeature?.let { feat ->
                        GoogleEarthInfoBalloon(
                            feature = feat,
                            onFlyTo = {
                                centerCameraOn(feat.center.latitude, feat.center.longitude, 4.5f)
                            },
                            onClose = { viewModel.selectKmlFeature(null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        }

        // --- Places & Feature Tree Bottom Sheet (Google Earth "My Places" panel) ---
        if (showPlacesDrawer && kmlDocument != null) {
            GoogleEarthPlacesSheet(
                doc = kmlDocument!!,
                selectedFeature = selectedFeature,
                onSelectFeature = { feat ->
                    viewModel.selectKmlFeature(feat)
                    centerCameraOn(feat.center.latitude, feat.center.longitude, 4.5f)
                    showPlacesDrawer = false
                },
                onToggleFeatureVisibility = { featId -> viewModel.toggleFeatureVisibility(featId) },
                onToggleFolderVisibility = { fName -> viewModel.toggleFolderVisibility(fName) },
                onSetAllLayersVisible = { visible -> viewModel.setAllLayersVisible(visible) },
                onDismiss = { showPlacesDrawer = false }
            )
        }

        // --- Google Earth Search & Find Sheet ---
        if (showSearchSheet && kmlDocument != null) {
            GoogleEarthSearchDialog(
                doc = kmlDocument!!,
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onSelectResult = { feat ->
                    viewModel.selectKmlFeature(feat)
                    centerCameraOn(feat.center.latitude, feat.center.longitude, 4.5f)
                    showSearchSheet = false
                },
                onDismiss = { showSearchSheet = false }
            )
        }

        // --- Paste KML XML Dialog ---
        if (showPasteXmlDialog) {
            AlertDialog(
                onDismissRequest = { showPasteXmlDialog = false },
                title = {
                    Text(
                        text = "Paste KML Content",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Paste XML string formatted with <kml> and <Document> or <Placemark> tags:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = pasteXmlInput,
                            onValueChange = { pasteXmlInput = it },
                            placeholder = { Text("<?xml ...>\n<kml>\n  <Document>...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (pasteXmlInput.isNotBlank()) {
                                viewModel.loadKmlString(pasteXmlInput)
                                showPasteXmlDialog = false
                                pasteXmlInput = ""
                                Toast.makeText(context, "KML parsed successfully", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text("Load KML")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPasteXmlDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

// ----------------------------------------------------------------------------------------
// UI Components: Top Bar, Empty Welcome Card, Info Balloon, Places Tree, Measure HUD
// ----------------------------------------------------------------------------------------

@Composable
private fun GoogleEarthTopBar(
    doc: KmlDocument,
    onPickNewKml: () -> Unit,
    onPasteXml: () -> Unit,
    onOpenSearch: () -> Unit,
    onTogglePlaces: () -> Unit,
    onToggleMeasure: () -> Unit,
    isMeasureMode: Boolean,
    basemapTheme: EarthBasemapTheme,
    onChangeTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Document Title & Stats
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00E5FF).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = "Earth",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = doc.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${doc.totalFeatures} places (${doc.totalPolygons} plots, ${doc.totalLines} paths, ${doc.totalPoints} pins)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Quick Actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Search
                IconButton(
                    onClick = onOpenSearch,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(18.dp))
                }

                // Layers & Places Hierarchy
                IconButton(
                    onClick = onTogglePlaces,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Layers, contentDescription = "KML Layers", modifier = Modifier.size(18.dp))
                }

                // Ruler / Measure Tool
                IconButton(
                    onClick = onToggleMeasure,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isMeasureMode) Color(0xFFFFD600).copy(alpha = 0.25f) else Color.Transparent)
                ) {
                    Icon(
                        Icons.Default.SquareFoot,
                        contentDescription = "Measure Tool",
                        tint = if (isMeasureMode) Color(0xFFFFD600) else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Switch Basemap Theme
                IconButton(
                    onClick = onChangeTheme,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = "Basemap Theme", modifier = Modifier.size(18.dp))
                }

                // Upload KML Button
                OutlinedButton(
                    onClick = onPickNewKml,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("KML", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

@Composable
private fun KmlUploadWelcomeCard(
    onPickFile: () -> Unit,
    onPasteXml: () -> Unit,
    onLoadSample: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = "Google Earth",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Google Earth KML Viewer",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Upload any standard KML or KMZ file to inspect boundary polygons, paths, canals, and waypoint placemarks with full Google Earth tools.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 1. Primary: Upload File
                Button(
                    onClick = onPickFile,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Upload KML / KMZ File", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Secondary: Paste XML
                OutlinedButton(
                    onClick = onPasteXml,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Paste KML Text", style = MaterialTheme.typography.labelLarge)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3. Instant Sample Project
                TextButton(
                    onClick = onLoadSample,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Load Sample Project", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
                }
            }
        }
    }
}

@Composable
private fun GoogleEarthInfoBalloon(
    feature: KmlFeature,
    onFlyTo: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f)),
        elevation = CardDefaults.cardElevation(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        modifier = modifier.widthIn(max = 560.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header: Type Icon, Title, Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    val (icon: ImageVector, tint: Color) = when (feature.geometryType) {
                        KmlGeometryType.POINT -> (Icons.Default.Place as ImageVector) to Color(0xFFFF5252)
                        KmlGeometryType.LINE_STRING -> (Icons.Default.Timeline as ImageVector) to Color(0xFF00E5FF)
                        KmlGeometryType.POLYGON -> (Icons.Default.Layers as ImageVector) to Color(0xFF00E676)
                        KmlGeometryType.MULTI_GEOMETRY -> (Icons.Default.Public as ImageVector) to Color(0xFFFFD600)
                    }
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(tint.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = feature.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (feature.folderName != null) {
                            Text(
                                text = "Folder: ${feature.folderName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                }
            }

            // Description
            if (!feature.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = feature.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Geospatial Metrics Table (Google Earth Specs)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    when (feature.geometryType) {
                        KmlGeometryType.POINT -> {
                            val pt = feature.center
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Coordinates:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    "%.6f°N, %.6f°E".format(pt.latitude, pt.longitude),
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("GPS DMS:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    GeoUtils.toDms(pt.latitude, pt.longitude),
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace)
                                )
                            }
                        }
                        KmlGeometryType.LINE_STRING -> {
                            val lengthM = feature.pathLengthMeters
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Path Length:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    "%,.1f m (%,.2f km)".format(lengthM, lengthM / 1000.0),
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                                    color = Color(0xFF00E5FF)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Vertices:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${feature.points.size} points", style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace))
                            }
                        }
                        KmlGeometryType.POLYGON -> {
                            val areaM2 = feature.polygonAreaMeters
                            val areaHa = feature.polygonAreaHectares
                            val perimeterM = feature.pathLengthMeters

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Enclosed Area:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    "%,.2f Ha (%,.0f m²)".format(areaHa, areaM2),
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                                    color = Color(0xFF00E676)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Perimeter:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    "%,.1f m (%,.2f km)".format(perimeterM, perimeterM / 1000.0),
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace)
                                )
                            }
                        }
                        else -> {}
                    }

                    // Extended Data Attributes
                    if (feature.extendedData.isNotEmpty()) {
                        feature.extendedData.forEach { (k, v) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("$k:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(v, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Actions: Fly To & Copy
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onFlyTo,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.NearMe, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Fly To Place")
                }

                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("KML Feature", "${feature.name}: ${feature.center.latitude}, ${feature.center.longitude}")
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Coordinates copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoogleEarthPlacesSheet(
    doc: KmlDocument,
    selectedFeature: KmlFeature?,
    onSelectFeature: (KmlFeature) -> Unit,
    onToggleFeatureVisibility: (String) -> Unit,
    onToggleFolderVisibility: (String) -> Unit,
    onSetAllLayersVisible: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .fillMaxHeight(0.80f)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Layers, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("KML Layers Hierarchy", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
                TextButton(onClick = onDismiss) {
                    Text("Done")
                }
            }

            Text(
                text = "${doc.totalFeatures} items across ${doc.folders.size} layers (Unrestricted visibility)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Master "All Layers Visible" Banner (No Restrictions)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E676))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "All Layers Visible",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "All plots, paths, and points 100% visible",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { onSetAllLayersVisible(true) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Show All", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Hierarchical Folder List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(doc.folders) { folder ->
                    var isExpanded by remember { mutableStateOf(true) }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            // Folder Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isExpanded = !isExpanded }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = folder.isVisible,
                                        onCheckedChange = { onToggleFolderVisibility(folder.name) },
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Icon(Icons.Default.Folder, contentDescription = null, tint = Color(0xFFFFD600), modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = folder.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "(${folder.features.size})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null
                                )
                            }

                            // Child Features
                            if (isExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 28.dp, end = 12.dp, bottom = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    folder.features.forEach { feat ->
                                        val isSelected = selectedFeature?.id == feat.id
                                        Surface(
                                            onClick = { onSelectFeature(feat) },
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Checkbox(
                                                        checked = feat.isVisible,
                                                        onCheckedChange = { onToggleFeatureVisibility(feat.id) },
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))

                                                    val geomIcon = when (feat.geometryType) {
                                                        KmlGeometryType.POINT -> Icons.Default.Place
                                                        KmlGeometryType.LINE_STRING -> Icons.Default.Timeline
                                                        KmlGeometryType.POLYGON -> Icons.Default.Layers
                                                        KmlGeometryType.MULTI_GEOMETRY -> Icons.Default.Public
                                                    }
                                                    Icon(geomIcon, contentDescription = null, tint = feat.style.lineColor, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = feat.name,
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                        ),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }

                                                Text(
                                                    text = when (feat.geometryType) {
                                                        KmlGeometryType.POLYGON -> "%,.1f Ha".format(feat.polygonAreaHectares)
                                                        KmlGeometryType.LINE_STRING -> "%,.0f m".format(feat.pathLengthMeters)
                                                        KmlGeometryType.POINT -> "Point"
                                                        KmlGeometryType.MULTI_GEOMETRY -> "Multi"
                                                    },
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontFamily = FontFamily.Monospace,
                                                        fontSize = 10.sp
                                                    ),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoogleEarthSearchDialog(
    doc: KmlDocument,
    query: String,
    onQueryChange: (String) -> Unit,
    onSelectResult: (KmlFeature) -> Unit,
    onDismiss: () -> Unit
) {
    val filtered = remember(query, doc) {
        if (query.isBlank()) doc.features
        else doc.features.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.description?.contains(query, ignoreCase = true) == true ||
            it.folderName?.contains(query, ignoreCase = true) == true
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .fillMaxHeight(0.7f)
        ) {
            Text(
                text = "Search in Project",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Search by name, canal, parcel, valve, coordinates...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "${filtered.size} matching places",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filtered) { feat ->
                    Surface(
                        onClick = { onSelectResult(feat) },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = feat.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = "${feat.folderName ?: "Places"} • ${feat.geometryType}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(Icons.Default.NearMe, contentDescription = "Fly To", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MeasurementHud(
    measurePoints: List<MeasurePoint>,
    onUndo: () -> Unit,
    onClear: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .padding(horizontal = 14.dp)
            .widthIn(max = 480.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1E293B).copy(alpha = 0.96f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD600).copy(alpha = 0.5f)),
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SquareFoot, contentDescription = null, tint = Color(0xFFFFD600), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Google Earth Ruler", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
                }
                IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close Ruler", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (measurePoints.isEmpty()) {
                Text(
                    text = "Tap on the map to place measurement markers.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray
                )
            } else {
                val totalDistMeters = if (measurePoints.size < 2) 0.0 else {
                    var sum = 0.0
                    for (i in 0 until measurePoints.size - 1) {
                        sum += GeoUtils.distanceMeters(
                            measurePoints[i].geoPoint.latitude, measurePoints[i].geoPoint.longitude,
                            measurePoints[i + 1].geoPoint.latitude, measurePoints[i + 1].geoPoint.longitude
                        )
                    }
                    sum
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Line Distance:", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                    Text(
                        "%,.1f m (%,.3f km)".format(totalDistMeters, totalDistMeters / 1000.0),
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color(0xFFFFD600))
                    )
                }

                if (measurePoints.size >= 3) {
                    val polygonArea = com.example.data.model.calculateSphericalPolygonArea(measurePoints.map { it.geoPoint })
                    val polygonHa = polygonArea / 10000.0
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Enclosed Area:", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                        Text(
                            "%,.2f Ha (%,.0f m²)".format(polygonHa, polygonArea),
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color(0xFF00E676))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (measurePoints.isNotEmpty()) {
                    TextButton(onClick = onUndo, contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)) {
                        Icon(Icons.Default.Undo, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Undo", color = Color.White, style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    TextButton(onClick = onClear, contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)) {
                        Text("Clear", color = Color(0xFFFF5252), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun CoordinatesScaleHud(
    center: GeoPoint,
    zoom: Float,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF0B132B).copy(alpha = 0.88f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            // Decimal coordinates & DMS
            Text(
                text = "%.5f°N, %.5f°E".format(center.latitude, center.longitude),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = Color.White
                )
            )

            // Approximate metric scale bar calculated based on zoom
            val metersPerPixel = (156543.03392 * cos(Math.toRadians(center.latitude)) / (zoom * 50.0)).coerceAtLeast(0.1)
            val barWidthPixels = 80.0
            val barMeters = barWidthPixels * metersPerPixel
            val formattedDistance = if (barMeters >= 1000) "%,.1f km".format(barMeters / 1000.0) else "%,.0f m".format(barMeters)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(64.dp)
                        .height(3.dp)
                        .background(Color.White)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = formattedDistance,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = Color(0xFF00E5FF)
                    )
                )
            }
        }
    }
}

// ----------------------------------------------------------------------------------------
// Canvas Drawing Helpers: Earth Basemap, Graticule Lines, Hit Testing Math
// ----------------------------------------------------------------------------------------

private fun DrawScope.drawEarthBasemap(
    theme: EarthBasemapTheme,
    width: Float,
    height: Float,
    density: Float,
    panX: Float,
    panY: Float,
    zoom: Float
) {
    when (theme) {
        EarthBasemapTheme.SATELLITE_SPACE -> {
            // Deep space globe atmosphere gradient
            drawRect(
                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                    colors = listOf(Color(0xFF132A46), Color(0xFF091424), Color(0xFF040810)),
                    center = Offset(width / 2f + panX * 0.1f, height / 2f + panY * 0.1f),
                    radius = max(width, height) * 0.85f
                ),
                size = Size(width, height)
            )

            // Subtle Earth Terrain Land Mass Simulation
            val tileW = 180f * density * zoom.coerceIn(0.5f, 2.5f)
            val tileH = 120f * density * zoom.coerceIn(0.5f, 2.5f)
            var px = (panX % tileW) - tileW
            var r = 0
            while (px < width + tileW) {
                var py = (panY % tileH) - tileH
                var c = 0
                while (py < height + tileH) {
                    val earthColors = listOf(
                        Color(0xFF1B3828), // Vegetation green
                        Color(0xFF264653), // Deep bay blue
                        Color(0xFF2A4736), // Forest green
                        Color(0xFF213A2B), // Farmland
                        Color(0xFF1D3557)  // Water body
                    )
                    val col = earthColors[(r * 3 + c) % earthColors.size].copy(alpha = 0.35f)
                    drawRect(
                        color = col,
                        topLeft = Offset(px + 4f * density, py + 4f * density),
                        size = Size(tileW - 8f * density, tileH - 8f * density)
                    )
                    py += tileH
                    c++
                }
                px += tileW
                r++
            }
        }
        EarthBasemapTheme.VECTOR_GIS -> {
            // Crisp dark technical GIS drafting canvas
            drawRect(color = Color(0xFF0F172A), size = Size(width, height))
            val minor = 24f * density
            val major = 96f * density

            var x = (panX % minor)
            while (x < width) {
                if (x >= 0) drawLine(Color(0xFF1E293B).copy(alpha = 0.5f), Offset(x, 0f), Offset(x, height), 0.7f * density)
                x += minor
            }
            var y = (panY % minor)
            while (y < height) {
                if (y >= 0) drawLine(Color(0xFF1E293B).copy(alpha = 0.5f), Offset(0f, y), Offset(width, y), 0.7f * density)
                y += minor
            }
        }
        EarthBasemapTheme.TERRAIN_TOPO -> {
            // Light topographic hypsometric relief
            drawRect(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(Color(0xFF1E3A5F), Color(0xFF1A3326), Color(0xFF382A1B)),
                    startY = 0f,
                    endY = height
                ),
                size = Size(width, height)
            )
        }
    }
}

private fun DrawScope.drawGraticuleGrid(
    width: Float,
    height: Float,
    bounds: com.example.data.model.KmlBounds,
    zoom: Float,
    density: Float,
    toScreen: (Double, Double) -> Offset
) {
    val graticuleColor = Color(0xFF38BDF8).copy(alpha = 0.15f)
    val graticuleStroke = 1f * density

    // Draw lines along latitude and longitude increments
    val step = (bounds.spanLat / 5.0).coerceAtLeast(0.001)
    var lat = bounds.minLat - bounds.spanLat
    while (lat <= bounds.maxLat + bounds.spanLat) {
        val pStart = toScreen(lat, bounds.minLng - bounds.spanLng * 2)
        val pEnd = toScreen(lat, bounds.maxLng + bounds.spanLng * 2)
        drawLine(
            color = graticuleColor,
            start = pStart,
            end = pEnd,
            strokeWidth = graticuleStroke,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
        )
        lat += step
    }

    var lng = bounds.minLng - bounds.spanLng
    while (lng <= bounds.maxLng + bounds.spanLng) {
        val pStart = toScreen(bounds.minLat - bounds.spanLat * 2, lng)
        val pEnd = toScreen(bounds.maxLat + bounds.spanLat * 2, lng)
        drawLine(
            color = graticuleColor,
            start = pStart,
            end = pEnd,
            strokeWidth = graticuleStroke,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
        )
        lng += step
    }
}

/**
 * Calculates perpendicular distance from tap point to line segment.
 */
private fun distanceToSegment(p: Offset, v: Offset, w: Offset): Float {
    val l2 = (v.x - w.x) * (v.x - w.x) + (v.y - w.y) * (v.y - w.y)
    if (l2 == 0f) return hypot(p.x - v.x, p.y - v.y)
    val t = (((p.x - v.x) * (w.x - v.x) + (p.y - v.y) * (w.y - v.y)) / l2).coerceIn(0f, 1f)
    val proj = Offset(v.x + t * (w.x - v.x), v.y + t * (w.y - v.y))
    return hypot(p.x - proj.x, p.y - proj.y)
}

/**
 * Standard Ray Casting algorithm for point-in-polygon test.
 */
private fun isPointInPolygon(tap: GeoPoint, polygon: List<GeoPoint>): Boolean {
    if (polygon.size < 3) return false
    var inside = false
    var j = polygon.size - 1
    for (i in polygon.indices) {
        val xi = polygon[i].longitude
        val yi = polygon[i].latitude
        val xj = polygon[j].longitude
        val yj = polygon[j].latitude
        val intersect = ((yi > tap.latitude) != (yj > tap.latitude)) &&
                (tap.longitude < (xj - xi) * (tap.latitude - yi) / (yj - yi) + xi)
        if (intersect) inside = !inside
        j = i
    }
    return inside
}
