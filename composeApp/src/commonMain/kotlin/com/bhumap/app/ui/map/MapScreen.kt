package com.bhumap.app.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bhumap.app.domain.model.Plot
import com.bhumap.app.ui.theme.*
import com.bhumap.app.utils.formatINR
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MapScreen() {
    val vm: MapViewModel = koinViewModel()
    val state by vm.state.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        // Full-screen map (platform-specific)
        PlatformMapView(
            plots        = state.plots,
            selectedPlot = state.selectedPlot,
            onPlotClick  = vm::onPlotSelected,
            modifier     = Modifier.fillMaxSize(),
        )

        // ─── Plot detail bottom sheet ─────────────────────────────────────────
        state.selectedPlot?.let { plot ->
            PlotDetailBottomCard(
                plot    = plot,
                onClose = { vm.onPlotSelected(null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
private fun PlotDetailBottomCard(
    plot: Plot,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier  = modifier.padding(12.dp),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Paper50),
        elevation = CardDefaults.cardElevation(8.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier     = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Plot ${plot.plotNumber}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Soil900, fontWeight = FontWeight.Bold,
                    ),
                )
                TextButton(onClick = onClose) { Text("Close", color = Soil500) }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PlotDetailChip("${plot.areaSqft} sq.ft")
                plot.totalPrice?.let { PlotDetailChip(it.formatINR()) }
                StatusBadge(plot.status.label, plot.status)
            }
            if (!plot.notes.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(plot.notes, style = MaterialTheme.typography.bodySmall.copy(color = Soil500))
            }
        }
    }
}

@Composable
private fun PlotDetailChip(label: String) {
    Surface(
        shape  = RoundedCornerShape(8.dp),
        color  = Paper100,
        border = androidx.compose.foundation.BorderStroke(1.dp, Paper200),
    ) {
        Text(
            label,
