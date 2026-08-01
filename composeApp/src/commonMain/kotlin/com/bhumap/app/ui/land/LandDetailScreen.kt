package com.bhumap.app.ui.land

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.bhumap.app.ui.theme.*
import com.bhumap.app.utils.formatINR
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandDetailScreen(landId: String, onBack: () -> Unit) {
    val vm: LandViewModel = koinViewModel()
    val state by vm.state.collectAsState()
    val land = state.lands.firstOrNull { it.id == landId }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Partners", "Plots")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(land?.name ?: "Land detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Evergreen),
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // ─── Tab row ──────────────────────────────────────────────────────
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor   = Evergreen,
                contentColor     = Paper50,
                indicator        = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Paper50,
                    )
                },
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTab == index,
                        onClick  = { selectedTab = index },
                        text     = {
                            Text(tab, color = if (selectedTab == index) Paper50 else Evergreen200)
                        },
                    )
                }
            }

            // ─── Tab content ──────────────────────────────────────────────────
            when (selectedTab) {
                0 -> LandOverviewTab(land)
                1 -> LandPartnersTab(landId)
                2 -> LandPlotsTab(landId)
            }
        }
    }
}

@Composable
private fun LandOverviewTab(land: com.bhumap.app.data.local.db.Land?) {
    if (land == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            CircularProgressIndicator(color = Evergreen)
        }
        return
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper50)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OverviewRow("Name",     land.name)
        OverviewRow("Location", land.location)
        OverviewRow("Area",     "${land.area_acres} acres")
        OverviewRow("Total cost", land.total_cost.formatINR())
        if (!land.notes.isNullOrBlank()) OverviewRow("Notes", land.notes)
    }
}

@Composable
private fun OverviewRow(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall.copy(color = Soil500))
        Text(value,  style = MaterialTheme.typography.bodyMedium.copy(color = Soil900, fontWeight = FontWeight.Medium))
        HorizontalDivider(color = Paper200, thickness = 1.dp, modifier = Modifier.padding(top = 10.dp))
    }
}

@Composable
private fun LandPartnersTab(landId: String) {
    // TODO: Load partners from PartnerRepository (Phase 2)
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
        Text("Partners — coming soon", color = Soil500)
    }
}

@Composable
private fun LandPlotsTab(landId: String) {
    // TODO: Load plots from PlotRepository
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
        Text("Plots — coming soon", color = Soil500)
    }
}
