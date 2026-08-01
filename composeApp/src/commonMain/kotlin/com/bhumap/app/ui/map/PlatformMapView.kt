package com.bhumap.app.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bhumap.app.domain.model.Plot

/**
 * Platform-specific interactive map composable.
 * Android actual → Google Maps SDK (maps-compose)
 * iOS actual     → MapKit via UIViewControllerRepresentable
 */
@Composable
expect fun PlatformMapView(
    plots: List<Plot>,
    selectedPlot: Plot?,
    onPlotClick: (Plot) -> Unit,
    modifier: Modifier,
)
