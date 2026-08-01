package com.bhumap.app.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
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
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@Composable
actual fun PlatformMapView(
    plots: List<Plot>,
    selectedPlot: Plot?,
    onPlotClick: (Plot) -> Unit,
    modifier: Modifier,
) {
    val context = LocalContext.current
    var mapViewInstance by remember { mutableStateOf<MapView?>(null) }
    var locationOverlayInstance by remember { mutableStateOf<MyLocationNewOverlay?>(null) }

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
                Configuration.getInstance().userAgentValue = ctx.packageName
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(15.0)

                    // Center on first plot's first coordinate, or Maharashtra geographic center
                    val centerPoint = plots.firstOrNull()?.boundaryJson?.let { parseBoundaryJson(it).firstOrNull() }
                        ?: GeoPoint(19.7515, 75.7139)
                    controller.setCenter(centerPoint)

                    val myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(ctx), this).apply {
                        if (hasLocationPermission()) {
                            enableMyLocation()
                        }
                    }
                    overlays.add(myLocationOverlay)
                    locationOverlayInstance = myLocationOverlay
                    mapViewInstance = this
                }
            },
            update = { mapView ->
                mapView.overlays.removeAll { it !is MyLocationNewOverlay }
                plots.forEach { plot ->
                    val points = parseBoundaryJson(plot.boundaryJson)
                    if (points.isNotEmpty()) {
                        val polygon = Polygon(mapView).apply {
                            this.points = points
                            fillColor = plot.status.mapFillColorInt()
                            strokeColor = plot.status.mapStrokeColorInt()
                            strokeWidth = if (plot == selectedPlot) 6f else 3f
                            title = plot.plotNumber
                            setOnClickListener { _, _, _ ->
                                onPlotClick(plot)
                                true
                            }
                        }
                        mapView.overlays.add(polygon)
                    }
                }
                mapView.invalidate()
            }
        )

        // ─── "My Location" Floating Action Button ─────────────────────────────
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

/** Parse simple GeoJSON Polygon coordinates array [[lng, lat], ...] into GeoPoints */
private fun parseBoundaryJson(json: String?): List<GeoPoint> {
    if (json.isNullOrBlank()) return emptyList()
    return try {
        val stripped = json.trim().removePrefix("[[").removeSuffix("]]")
        stripped.split("],[").map { pair ->
            val (lng, lat) = pair.split(",").map { it.trim().toDouble() }
            GeoPoint(lat, lng)
        }
    } catch (_: Exception) {
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

// Stroke = same hue, full opacity (alpha 255), width 3f
private fun PlotStatus.mapStrokeColorInt(): Int = when (this) {
    PlotStatus.AVAILABLE    -> Color.rgb(0x22, 0xC5, 0x5E)  // #22C55E
    PlotStatus.RESERVED     -> Color.rgb(0xF5, 0x9E, 0x0B)  // #F59E0B
    PlotStatus.SOLD_PENDING -> Color.rgb(0xEF, 0x44, 0x44)  // #EF4444
    PlotStatus.SOLD_PAID    -> Color.rgb(0x99, 0x1B, 0x1B)  // #991B1B
    PlotStatus.BLOCKED      -> Color.rgb(0x6B, 0x72, 0x80)  // #6B7280
}
