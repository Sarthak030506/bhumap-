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
