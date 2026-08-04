package com.bhumap.app.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bhumap.app.domain.model.Plot

/**
 * Platform-specific interactive map composable with drawing support.
 * Android actual → OpenStreetMap via osmdroid with satellite tile source
 * iOS actual     → MapKit via UIKitView
 */
@Composable
expect fun PlatformMapView(
    plots: List<Plot>,
    selectedPlot: Plot?,
    onPlotClick: (Plot) -> Unit,
    isDrawing: Boolean = false,
    drawingPoints: List<MapPoint> = emptyList(),
    onAddPoint: (MapPoint) -> Unit = {},
    modifier: Modifier = Modifier,
)
