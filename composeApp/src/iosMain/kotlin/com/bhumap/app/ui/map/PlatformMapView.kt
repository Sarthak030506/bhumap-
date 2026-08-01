package com.bhumap.app.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.bhumap.app.domain.model.Plot
import com.bhumap.app.ui.theme.Paper50
import com.bhumap.app.ui.theme.Soil500
import platform.MapKit.*
import platform.CoreLocation.CLLocationCoordinate2DMake
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PlatformMapView(
    plots: List<Plot>,
    selectedPlot: Plot?,
    onPlotClick: (Plot) -> Unit,
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

            // Center map on Nashik, Maharashtra as default
            val region = MKCoordinateRegionMakeWithDistance(
                CLLocationCoordinate2DMake(19.9975, 73.7898),
                50_000.0,
                50_000.0,
            )
            mapView.setRegion(region, false)
            mapView
        },
        update = { /* React to state changes if needed */ },
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
