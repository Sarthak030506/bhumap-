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

            // Center map on Maharashtra default
            val region = MKCoordinateRegionMakeWithDistance(
                CLLocationCoordinate2DMake(19.7515, 75.7139),
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
    } catch (_: Exception) {
        emptyList()
    }
}
