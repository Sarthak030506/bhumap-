package com.bhumap.app.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.bhumap.app.domain.model.Plot
import com.bhumap.app.domain.model.PlotStatus
import com.bhumap.app.ui.theme.Evergreen
import com.bhumap.app.ui.theme.Paper50
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

import android.preference.PreferenceManager
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.MapTileIndex

/** Google Satellite Tile Source (High-resolution India rural coverage up to max zoom 20) */
private val GOOGLE_SATELLITE_TILE_SOURCE = object : OnlineTileSourceBase(
    "GoogleSatellite",
    1,
    20,
    256,
    "",
    arrayOf(
        "https://mt0.google.com/vt/lyrs=s",
        "https://mt1.google.com/vt/lyrs=s",
        "https://mt2.google.com/vt/lyrs=s",
        "https://mt3.google.com/vt/lyrs=s"
    ),
    "© Google"
) {
    override fun getTileURLString(pMapTileIndex: Long): String {
        val z = MapTileIndex.getZoom(pMapTileIndex)
        val x = MapTileIndex.getX(pMapTileIndex)
        val y = MapTileIndex.getY(pMapTileIndex)
        return "$baseUrl&x=$x&y=$y&z=$z"
    }
}

@Composable
actual fun PlatformMapView(
    plots: List<Plot>,
    selectedPlot: Plot?,
    onPlotClick: (Plot) -> Unit,
    isDrawing: Boolean,
    drawingPoints: List<MapPoint>,
    onAddPoint: (MapPoint) -> Unit,
    savedCenter: MapPoint?,
    savedZoom: Double,
    onCameraMoved: (center: MapPoint, zoom: Double) -> Unit,
    modifier: Modifier,
) {
    val context = LocalContext.current
    var mapViewInstance by remember { mutableStateOf<MapView?>(null) }
    var locationOverlayInstance by remember { mutableStateOf<MyLocationNewOverlay?>(null) }
    var hasCenteredOnPlots by remember { mutableStateOf(false) }

    // Maintain current drawing state in refs to avoid recreate overhead inside event callbacks
    val currentIsDrawing by rememberUpdatedState(isDrawing)
    val currentOnAddPoint by rememberUpdatedState(onAddPoint)
    val currentOnCameraMoved by rememberUpdatedState(onCameraMoved)

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            locationOverlayInstance?.enableMyLocation()
            locationOverlayInstance?.enableFollowLocation()
            locationOverlayInstance?.runOnFirstFix {
                val loc = locationOverlayInstance?.myLocation
                if (loc != null && mapViewInstance != null) {
                    mapViewInstance?.post {
                        mapViewInstance?.controller?.animateTo(loc, 16.5, 1000L)
                    }
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                Configuration.getInstance().apply {
                    load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
                    userAgentValue = "BhuMap/1.0 (Android)"
                    osmdroidBasePath = java.io.File(ctx.cacheDir, "osmdroid")
                    osmdroidTileCache = java.io.File(ctx.cacheDir, "osmdroid/tiles")
                }
                MapView(ctx).apply {
                    setTileSource(GOOGLE_SATELLITE_TILE_SOURCE)
                    setMultiTouchControls(true)

                    // Remove outdated default osmdroid zoom +/- buttons
                    zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)

                    // FIX 4: Restore saved viewport from ViewModel, or use Maharashtra default
                    if (savedCenter != null) {
                        controller.setZoom(savedZoom)
                        controller.setCenter(GeoPoint(savedCenter.lat, savedCenter.lng))
                        hasCenteredOnPlots = true // Skip auto-center since we have saved viewport
                    } else {
                        controller.setZoom(7.0)
                        controller.setCenter(GeoPoint(19.7515, 75.7139))
                    }

                    val myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(ctx), this).apply {
                        if (hasLocationPermission()) {
                            enableMyLocation()
                        }
                    }
                    overlays.add(myLocationOverlay)
                    locationOverlayInstance = myLocationOverlay
                    mapViewInstance = this

                    // FIX 4: Track camera moves and save to ViewModel state
                    addMapListener(object : MapListener {
                        override fun onScroll(event: ScrollEvent?): Boolean {
                            val c = mapCenter
                            currentOnCameraMoved(MapPoint(c.latitude, c.longitude), zoomLevelDouble)
                            return false
                        }
                        override fun onZoom(event: ZoomEvent?): Boolean {
                            val c = mapCenter
                            currentOnCameraMoved(MapPoint(c.latitude, c.longitude), zoomLevelDouble)
                            return false
                        }
                    })
                }
            },
            update = { mapView ->
                // Clear existing plot/drawing overlays except location overlay
                mapView.overlays.removeAll { it !is MyLocationNewOverlay }

                // ─── Drawing Mode Overlays ─────────────────────────────────────────────
                if (currentIsDrawing) {
                    val eventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                            if (currentIsDrawing) {
                                currentOnAddPoint(MapPoint(p.latitude, p.longitude))
                                return true
                            }
                            return false
                        }
                        override fun longPressHelper(p: GeoPoint): Boolean = false
                    })
                    mapView.overlays.add(eventsOverlay)

                    // Draw connecting lines between points
                    if (drawingPoints.size >= 2) {
                        val polyline = Polyline(mapView).apply {
                            setPoints(drawingPoints.map { GeoPoint(it.lat, it.lng) })
                            outlinePaint.color = Color.rgb(0x22, 0xC5, 0x5E) // Bright Green
                            outlinePaint.strokeWidth = 6f
                            outlinePaint.strokeCap = Paint.Cap.ROUND
                        }
                        mapView.overlays.add(polyline)
                    }

                    // If >= 3 points, draw draft filled polygon
                    if (drawingPoints.size >= 3) {
                        val draftPolygon = Polygon(mapView).apply {
                            points = drawingPoints.map { GeoPoint(it.lat, it.lng) }
                            fillColor = Color.argb(80, 0x22, 0xC5, 0x5E)
                            strokeColor = Color.rgb(0x22, 0xC5, 0x5E)
                            strokeWidth = 4f
                        }
                        mapView.overlays.add(draftPolygon)
                    }

                    // Place markers at each tapped point
                    drawingPoints.forEachIndexed { index, pt ->
                        val marker = Marker(mapView).apply {
                            position = GeoPoint(pt.lat, pt.lng)
                            title = "Point ${index + 1}"
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        }
                        mapView.overlays.add(marker)
                    }

                } else {
                    // ─── Normal View Mode: Render Saved Plots ────────────────────────
                    var firstPlotCenter: GeoPoint? = null

                    plots.forEach { plot ->
                        val points = parseBoundaryJson(plot.boundaryJson)
                        if (points.isNotEmpty()) {
                            if (firstPlotCenter == null) {
                                firstPlotCenter = points.first()
                            }
                            val polygon = Polygon(mapView).apply {
                                this.points = points
                                fillColor = plot.status.mapFillColorInt()
                                strokeColor = plot.status.mapStrokeColorInt()
                                strokeWidth = if (plot == selectedPlot) 7f else 4f
                                title = plot.plotNumber
                                setOnClickListener { _, _, _ ->
                                    onPlotClick(plot)
                                    true
                                }
                            }
                            mapView.overlays.add(polygon)
                        }
                    }

                    // Auto-center map on plots when loaded (first time plots arrive)
                    // Skip if we already restored a saved viewport from ViewModel
                    if (!hasCenteredOnPlots && firstPlotCenter != null) {
                        hasCenteredOnPlots = true
                        mapView.controller.animateTo(firstPlotCenter, 16.5, 800L)
                    }
                }

                mapView.invalidate()
            }
        )

        // ─── "My Location" Floating Action Button (Top-Right) ────────────────
        FloatingActionButton(
            onClick = {
                if (!hasLocationPermission()) {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                } else {
                    val locOverlay = locationOverlayInstance
                    val map = mapViewInstance
                    locOverlay?.enableMyLocation()
                    locOverlay?.enableFollowLocation()

                    val loc = locOverlay?.myLocation
                    if (loc != null && map != null) {
                        map.controller.animateTo(loc, 16.5, 1000L)
                    } else {
                        locOverlay?.runOnFirstFix {
                            val firstFixLoc = locOverlay.myLocation
                            if (firstFixLoc != null && map != null) {
                                map.post {
                                    map.controller.animateTo(firstFixLoc, 16.5, 1000L)
                                }
                            }
                        }
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp),
            shape = CircleShape,
            containerColor = Paper50,
            contentColor = Evergreen,
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "My Location",
            )
        }
    }
}

/**
 * Robust boundary parser supporting both [[lng,lat],...] and [{"lat":..., "lng":...}] formats.
 * FIX 5: Parse failures are logged with raw input for diagnostics instead of silent swallow.
 */
private fun parseBoundaryJson(json: String?): List<GeoPoint> {
    if (json.isNullOrBlank()) return emptyList()
    return try {
        val trimmed = json.trim()
        when {
            trimmed.startsWith("[[") -> {
                val stripped = trimmed.removePrefix("[[").removeSuffix("]]")
                stripped.split("],[").mapNotNull { pair ->
                    val parts = pair.split(",").map { it.trim().toDouble() }
                    if (parts.size >= 2) GeoPoint(parts[1], parts[0]) else null
                }
            }
            trimmed.contains("\"lat\"") || trimmed.contains("\"latitude\"") -> {
                val arr = Json.parseToJsonElement(trimmed).jsonArray
                arr.mapNotNull { el ->
                    val obj = el.jsonObject
                    val lat = obj["lat"]?.jsonPrimitive?.doubleOrNull ?: obj["latitude"]?.jsonPrimitive?.doubleOrNull
                    val lng = obj["lng"]?.jsonPrimitive?.doubleOrNull ?: obj["longitude"]?.jsonPrimitive?.doubleOrNull
                    if (lat != null && lng != null) GeoPoint(lat, lng) else null
                }
            }
            else -> {
                println("BhumapApp: boundary parse skipped — unrecognized format: ${trimmed.take(80)}")
                emptyList()
            }
        }
    } catch (e: Exception) {
        // FIX 5: Never swallow parse failures silently — log for diagnostics
        println("BhumapApp: boundary parse FAILED — raw: ${json.take(120)} — error: ${e.message}")
        emptyList()
    }
}

// alpha = 128 ≈ 50% opacity as per spec (#RRGGBB from plots_view status_color palette)
private fun PlotStatus.mapFillColorInt(): Int = when (this) {
    PlotStatus.AVAILABLE    -> Color.argb(128, 0x22, 0xC5, 0x5E)  // #22C55E
    PlotStatus.RESERVED     -> Color.argb(128, 0xF5, 0x9E, 0x0B)  // #F59E0B
    PlotStatus.SOLD_PENDING -> Color.argb(128, 0xEF, 0x44, 0x44)  // #EF4444
    PlotStatus.SOLD_PAID    -> Color.argb(128, 0x99, 0x1B, 0x1B)  // #991B1B
    PlotStatus.BLOCKED      -> Color.argb(128, 0x6B, 0x72, 0x80)  // #6B7280
}

// Stroke = same hue, full opacity (alpha 255), width 4f
private fun PlotStatus.mapStrokeColorInt(): Int = when (this) {
    PlotStatus.AVAILABLE    -> Color.rgb(0x22, 0xC5, 0x5E)  // #22C55E
    PlotStatus.RESERVED     -> Color.rgb(0xF5, 0x9E, 0x0B)  // #F59E0B
    PlotStatus.SOLD_PENDING -> Color.rgb(0xEF, 0x44, 0x44)  // #EF4444
    PlotStatus.SOLD_PAID    -> Color.rgb(0x99, 0x1B, 0x1B)  // #991B1B
    PlotStatus.BLOCKED      -> Color.rgb(0x6B, 0x72, 0x80)  // #6B7280
}
