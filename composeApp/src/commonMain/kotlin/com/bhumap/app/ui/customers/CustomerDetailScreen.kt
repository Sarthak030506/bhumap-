package com.bhumap.app.ui.customers

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bhumap.app.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(customerId: String, onBack: () -> Unit) {
    val vm: CustomerViewModel = koinViewModel()
    val state by vm.state.collectAsState()
    val customer = state.customers.firstOrNull { it.id == customerId }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Plot & Sale", "Payments", "EMI Schedule")
