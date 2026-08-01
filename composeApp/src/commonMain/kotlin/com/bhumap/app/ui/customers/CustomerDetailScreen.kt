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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(customer?.name ?: "Customer") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Evergreen),
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor   = Evergreen,
                contentColor     = Paper50,
            ) {
                tabs.forEachIndexed { i, tab ->
                    Tab(
                        selected = selectedTab == i,
                        onClick  = { selectedTab = i },
                        text     = { Text(tab, color = if (selectedTab == i) Paper50 else Evergreen200) },
                    )
                }
            }
            when (selectedTab) {
                0 -> PlotSaleTab(customer)
                1 -> PaymentsTab()
                2 -> EmiScheduleTab()
            }
        }
    }
}

@Composable
private fun PlotSaleTab(customer: com.bhumap.app.data.local.db.Customer?) {
    if (customer == null) { LoadingBox(); return }
    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        DetailRow("Name",    customer.name)
        DetailRow("Phone",   customer.phone)
        customer.email?.let   { DetailRow("Email",   it) }
        customer.aadhaar?.let { DetailRow("Aadhaar", it) }
        customer.address?.let { DetailRow("Address", it) }
        // TODO: Link to Sale record
    }
}

@Composable private fun PaymentsTab() {
    Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Payments — coming soon", color = Soil500) }
}
@Composable private fun EmiScheduleTab() {
    Box(Modifier.fillMaxSize(), Alignment.Center) { Text("EMI Schedule — coming soon", color = Soil500) }
}
@Composable private fun LoadingBox() {
    Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = Evergreen) }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(bottom = 14.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall.copy(color = Soil500))
        Text(value,  style = MaterialTheme.typography.bodyMedium.copy(color = Soil900, fontWeight = FontWeight.Medium))
        HorizontalDivider(color = Paper200, modifier = Modifier.padding(top = 10.dp))
    }
}
