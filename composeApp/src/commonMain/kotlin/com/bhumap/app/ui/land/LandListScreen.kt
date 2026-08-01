package com.bhumap.app.ui.land

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bhumap.app.data.local.db.Land
import com.bhumap.app.ui.theme.*
import com.bhumap.app.utils.formatINR
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandListScreen(onAdd: () -> Unit, onSelect: (String) -> Unit) {
    val vm: LandViewModel = koinViewModel()
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Lands", style = MaterialTheme.typography.titleLarge.copy(color = Soil900))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Paper50),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick          = onAdd,
                containerColor   = Evergreen,
                contentColor     = Paper50,
                shape            = CircleShape,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add land")
            }
        },
        containerColor = Paper50,
    ) { padding ->
        when {
            state.isLoading -> LandListSkeleton(padding)
            state.lands.isEmpty() -> LandEmptyState(padding, onAdd)
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp,
                        top = padding.calculateTopPadding() + 8.dp,
                        bottom = padding.calculateBottomPadding() + 80.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.lands, key = { it.id }) { land ->
                        LandCard(land = land, onClick = { onSelect(land.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun LandCard(land: Land, onClick: () -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = Paper50),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border    = androidx.compose.foundation.BorderStroke(1.dp, Paper200),
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Evergreen50),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Landscape, null, tint = Evergreen, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    land.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Soil900, fontWeight = FontWeight.SemiBold,
                    ),
                )
                Text(
                    land.location,
                    style = MaterialTheme.typography.bodySmall.copy(color = Soil500),
                )
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "${land.area_acres} acres",
                        style = MaterialTheme.typography.labelMedium.copy(color = Soil700),
                    )
                    Text(
                        land.total_cost.formatINR(),
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Evergreen, fontWeight = FontWeight.Medium,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun LandEmptyState(padding: PaddingValues, onAdd: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Filled.Landscape,
                contentDescription = null,
                tint = Soil300,
                modifier = Modifier.size(80.dp),
            )
            Spacer(Modifier.height(16.dp))
            Text("No lands added yet", style = MaterialTheme.typography.titleMedium.copy(color = Soil700))
            Spacer(Modifier.height(8.dp))
            Text(
                "Tap + to add your first land parcel",
                style = MaterialTheme.typography.bodyMedium.copy(color = Soil500),
