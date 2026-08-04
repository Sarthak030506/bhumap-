package com.bhumap.app.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import com.bhumap.app.domain.model.Plot
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.*

@OptIn(ExperimentalForeignApi::class)
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
    UIKitView(
        modifier = modifier.fillMaxSize(),
        factory  = {
            val mapView = MKMapView()
            mapView.showsUserLocation = false

            // Add polygon overlays for each plot with boundary
            plots.forEach { plot ->
                val coords = parseBoundaryCoords(plot.boundaryJson)
                if (coords.isNotEmpty()) {
                    val polygon = MKPolygon.polygonWithCoordinates(coords, coords.size.toULong())
                    mapView.addOverlay(polygon)
                }
            }

            // Center map on saved viewport or Maharashtra default
            val centerLat = savedCenter?.lat ?: 19.7515
            val centerLng = savedCenter?.lng ?: 75.7139
            val region = MKCoordinateRegionMakeWithDistance(
                CLLocationCoordinate2DMake(centerLat, centerLng),
                500_000.0,
                500_000.0,
            )
            mapView.setRegion(region, false)
            mapView
        },
        update = { /* React to state changes */ },
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun parseBoundaryCoords(
    json: String?,
): List<platform.CoreLocation.CLLocationCoordinate2D> {
    if (json.isNullOrBlank()) return emptyList()
    return try {
        val stripped = json.trim().removePrefix("[[").removeSuffix("]]")
        stripped.split("],[").map { pair ->
            val (lng, lat) = pair.split(",").map { it.trim().toDouble() }
            CLLocationCoordinate2DMake(lat, lng)
        }
    } catch (e: Exception) {
        // FIX 5: Log parse failures instead of silent swallow
        println("BhumapApp iOS: boundary parse FAILED — raw: ${json.take(120)} — error: ${e.message}")
        emptyList()
    }
}
