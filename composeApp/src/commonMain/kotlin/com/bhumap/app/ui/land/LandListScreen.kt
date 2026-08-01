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
