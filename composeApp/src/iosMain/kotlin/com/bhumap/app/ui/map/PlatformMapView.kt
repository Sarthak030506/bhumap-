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
