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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
                1 -> LandPartnersTab(landId, vm)
                2 -> LandPlotsTab(landId, vm)
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
private fun LandPartnersTab(landId: String, vm: LandViewModel) {
    val partners by remember(landId) { vm.observePartners(landId) }.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Paper50)) {
        if (partners.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    "No Partners Added",
                    style = MaterialTheme.typography.titleMedium.copy(color = Soil900, fontWeight = FontWeight.Bold),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Add investors or land development partners to track profit share and commitments.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Soil500),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Evergreen),
                ) {
                    Text("Add Partner")
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                ) {
                    Text(
                        "${partners.size} Partners",
                        style = MaterialTheme.typography.titleMedium.copy(color = Soil900, fontWeight = FontWeight.Bold),
                    )
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Evergreen),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    ) {
                        Text("+ Add Partner", style = MaterialTheme.typography.labelMedium)
                    }
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(partners, key = { it.id }) { partner ->
                        PartnerCard(partner)
                    }
                }
            }
        }

        if (showAddDialog) {
            AddPartnerDialog(
                onDismiss = { showAddDialog = false },
                onSave = { name, phone, committed, paid, pct, notes ->
                    vm.savePartner(
                        landId = landId,
                        name = name,
                        phone = phone,
                        committedAmount = committed,
                        paidAmount = paid,
                        profitSharePct = pct,
                        notes = notes,
                        onSuccess = { showAddDialog = false },
                        onFailure = {},
                    )
                },
            )
        }
    }
}

@Composable
private fun PartnerCard(partner: com.bhumap.app.domain.model.Partner) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Paper100),
        border = androidx.compose.foundation.BorderStroke(1.dp, Paper200),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                Column {
                    Text(partner.name, style = MaterialTheme.typography.titleMedium.copy(color = Soil900, fontWeight = FontWeight.Bold))
                    Text(partner.phone, style = MaterialTheme.typography.bodySmall.copy(color = Soil500))
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Evergreen50,
                ) {
                    Text(
                        "${partner.profitSharePct}% Share",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium.copy(color = Evergreen, fontWeight = FontWeight.Bold),
                    )
                }
            }

            HorizontalDivider(color = Paper200)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text("Committed", style = MaterialTheme.typography.labelSmall.copy(color = Soil500))
                    Text(partner.committedAmount.formatINR(), style = MaterialTheme.typography.bodyMedium.copy(color = Soil900, fontWeight = FontWeight.SemiBold))
                }
                Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                    Text("Paid Amount", style = MaterialTheme.typography.labelSmall.copy(color = Soil500))
                    Text(partner.paidAmount.formatINR(), style = MaterialTheme.typography.bodyMedium.copy(color = Evergreen, fontWeight = FontWeight.SemiBold))
                }
            }

            if (!partner.notes.isNullOrBlank()) {
                Text(
                    partner.notes,
                    style = MaterialTheme.typography.bodySmall.copy(color = Soil500),
                    maxLines = 2,
                )
            }
        }
    }
}

@Composable
private fun AddPartnerDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, committed: Double, paid: Double, pct: Double, notes: String?) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var committed by remember { mutableStateOf("") }
    var paid by remember { mutableStateOf("") }
    var pct by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Partner / Investor", style = MaterialTheme.typography.titleLarge.copy(color = Soil900)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Partner Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = committed,
                    onValueChange = { committed = it },
                    label = { Text("Committed Amount (₹)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = paid,
                    onValueChange = { paid = it },
                    label = { Text("Paid Amount (₹)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = pct,
                    onValueChange = { pct = it },
                    label = { Text("Profit Share %") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                )

                errorMsg?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) { errorMsg = "Name is required"; return@Button }
                    if (phone.isBlank()) { errorMsg = "Phone is required"; return@Button }
                    val committedVal = committed.toDoubleOrNull() ?: 0.0
                    val paidVal = paid.toDoubleOrNull() ?: 0.0
                    val pctVal = pct.toDoubleOrNull() ?: 0.0
                    onSave(name, phone, committedVal, paidVal, pctVal, notes.ifBlank { null })
                },
                colors = ButtonDefaults.buttonColors(containerColor = Evergreen),
            ) {
                Text("Save Partner")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Soil500) }
        },
    )
}

@Composable
private fun LandPlotsTab(landId: String, vm: LandViewModel) {
    val plots by remember(landId) { vm.observePlots(landId) }.collectAsState(initial = emptyList())

    Box(modifier = Modifier.fillMaxSize().background(Paper50)) {
        if (plots.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    "No Plots Drawn Yet",
                    style = MaterialTheme.typography.titleMedium.copy(color = Soil900, fontWeight = FontWeight.Bold),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Switch to the Map tab to draw and save plot boundaries within this land.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Soil500),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                // Summary bar
                val totalPlots = plots.size
                val available = plots.count { it.status == com.bhumap.app.domain.model.PlotStatus.AVAILABLE }
                val reserved = plots.count { it.status == com.bhumap.app.domain.model.PlotStatus.RESERVED }
                val sold = plots.count { it.status == com.bhumap.app.domain.model.PlotStatus.SOLD_PENDING || it.status == com.bhumap.app.domain.model.PlotStatus.SOLD_PAID }

                Surface(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Paper100,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Paper200),
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                    ) {
                        StatColumn("Total Plots", "$totalPlots")
                        StatColumn("Available", "$available", Evergreen)
                        StatColumn("Reserved", "$reserved", Amber500)
                        StatColumn("Sold", "$sold", Terracotta)
                    }
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(plots, key = { it.id }) { plot ->
                        PlotItemCard(plot)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String, color: androidx.compose.ui.graphics.Color = Soil900) {
    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium.copy(color = color, fontWeight = FontWeight.Bold))
        Text(label, style = MaterialTheme.typography.labelSmall.copy(color = Soil500))
    }
}

@Composable
private fun PlotItemCard(plot: com.bhumap.app.domain.model.Plot) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Paper100),
        border = androidx.compose.foundation.BorderStroke(1.dp, Paper200),
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Plot ${plot.plotNumber}",
                    style = MaterialTheme.typography.titleMedium.copy(color = Soil900, fontWeight = FontWeight.Bold),
                )
                Text(
                    "${plot.areaSqft} sq.ft",
                    style = MaterialTheme.typography.bodySmall.copy(color = Soil500),
                )
                plot.pricePerSqft?.let { price ->
                    Text(
                        "₹$price / sq.ft • Total: ${plot.totalPrice?.formatINR() ?: "-"}",
                        style = MaterialTheme.typography.bodySmall.copy(color = Soil700, fontWeight = FontWeight.Medium),
                    )
                }
            }

            val (bgColor, fgColor) = when (plot.status) {
                com.bhumap.app.domain.model.PlotStatus.AVAILABLE -> Evergreen50 to Evergreen
                com.bhumap.app.domain.model.PlotStatus.RESERVED -> androidx.compose.ui.graphics.Color(0xFFFFFBEB) to Amber500
                com.bhumap.app.domain.model.PlotStatus.SOLD_PENDING -> androidx.compose.ui.graphics.Color(0xFFFFF7ED) to Orange500
                com.bhumap.app.domain.model.PlotStatus.SOLD_PAID -> Terracotta50 to Terracotta
                com.bhumap.app.domain.model.PlotStatus.BLOCKED -> androidx.compose.ui.graphics.Color(0xFFF1F5F9) to Slate500
            }

            Surface(shape = RoundedCornerShape(8.dp), color = bgColor) {
                Text(
                    plot.status.label,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium.copy(color = fgColor, fontWeight = FontWeight.Medium),
                )
            }
        }
    }
}

