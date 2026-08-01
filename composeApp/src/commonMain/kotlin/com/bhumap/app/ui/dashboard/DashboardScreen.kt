package com.bhumap.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bhumap.app.ui.theme.*
import com.bhumap.app.utils.formatINRCompact
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
    val vm: DashboardViewModel = koinViewModel()
    val stats by vm.stats.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper50),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        // ─── Header gradient ──────────────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.verticalGradient(listOf(Evergreen, Evergreen400))
                    )
                    .padding(horizontal = 24.dp, vertical = 32.dp),
            ) {
                Column {
                    Text(
                        "Good morning 👋",
                        style = MaterialTheme.typography.bodyLarge.copy(color = Evergreen200),
                    )
                    Text(
                        "BhuMap",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = Paper50, fontWeight = FontWeight.Bold,
                        ),
                    )
                    Text(
                        "Apni Zameen, Apna Hisaab",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Evergreen200),
                    )
                }
            }
        }

        // ─── KPI cards grid ───────────────────────────────────────────────────
        item {
            if (stats.isLoading) {
                KpiShimmer()
            } else {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .offset(y = (-20).dp),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        KpiCard(
                            modifier = Modifier.weight(1f),
                            icon     = Icons.Filled.Landscape,
                            label    = "Lands",
                            value    = stats.totalLands.toString(),
                            tint     = Evergreen,
                            bg       = Evergreen50,
                        )
                        KpiCard(
                            modifier = Modifier.weight(1f),
                            icon     = Icons.Filled.People,
                            label    = "Customers",
                            value    = stats.totalCustomers.toString(),
                            tint     = Evergreen700,
                            bg       = Evergreen50,
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        KpiCard(
                            modifier = Modifier.weight(1f),
                            icon     = Icons.Filled.CurrencyRupee,
                            label    = "Portfolio",
                            value    = stats.totalRevenue.formatINRCompact(),
                            tint     = Terracotta,
                            bg       = Terracotta50,
                        )
                        KpiCard(
                            modifier = Modifier.weight(1f),
                            icon     = Icons.Filled.Notifications,
                            label    = "Pending EMIs",
                            value    = stats.pendingEmis.toString(),
                            tint     = Amber500,
                            bg       = Color(0xFFFFFBEB),
                        )
