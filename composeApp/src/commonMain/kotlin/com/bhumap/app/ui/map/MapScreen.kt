package com.bhumap.app.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bhumap.app.domain.model.Land
import com.bhumap.app.domain.model.Plot
import com.bhumap.app.domain.model.PlotStatus
import com.bhumap.app.ui.theme.*
import com.bhumap.app.utils.formatINR
import org.koin.compose.viewmodel.koinViewModel

// Semi-transparent dark background (#1A1A1A at 80% opacity)
private val DarkOverlayBg = Color(0xCC1A1A1A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onNavigateToLand: (() -> Unit)? = null,
    onNavigateToAddLand: ((boundaryJson: String) -> Unit)? = null,
) {
    val vm: MapViewModel = koinViewModel()
    val state by vm.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // ─── Show error as Snackbar, then clear ──────────────────────────────────
    LaunchedEffect(state.error) {
        val msg = state.error ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(
            message = msg,
            duration = SnackbarDuration.Long,
        )
        vm.clearError()
    }

    // Calculate plot stats for Top-Left Card
    val availableCount = state.plots.count { it.status == PlotStatus.AVAILABLE }
    val reservedCount = state.plots.count { it.status == PlotStatus.RESERVED }
    val soldCount = state.plots.count { it.status == PlotStatus.SOLD_PENDING || it.status == PlotStatus.SOLD_PAID }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Color(0xFF1A1A1A),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp),
                )
            }
        },
        containerColor = Color.Transparent,
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            // ─── Satellite Map View ───────────────────────────────────────────
            PlatformMapView(
                plots = state.plots,
                lands = state.lands,
                selectedPlot = state.selectedPlot,
                onPlotClick = vm::onPlotSelected,
                isDrawing = state.isDrawing,
                drawingPoints = state.drawingPoints,
                onAddPoint = vm::addDrawingPoint,
                savedCenter = state.mapCenter,
                savedZoom = state.mapZoom,
                onCameraMoved = vm::onMapCameraMoved,
                modifier = Modifier.fillMaxSize(),
            )

            // ─── TOP-LEFT Floating KPI Card ───────────────────────────────────
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 16.dp, start = 16.dp),
                shape = RoundedCornerShape(12.dp),
                color = DarkOverlayBg,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StatusCountItem(Color(0xFF22C55E), "$availableCount Available")
                    StatusCountItem(Color(0xFFF59E0B), "$reservedCount Reserved")
                    StatusCountItem(Color(0xFFEF4444), "$soldCount Sold")
                }
            }

            // ─── BOTTOM-LEFT Legend Card ──────────────────────────────────────
            if (!state.isDrawing && state.selectedPlot == null) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 24.dp, start = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = DarkOverlayBg,
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            "LEGEND",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.LightGray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                            ),
                        )
                        LegendItem(Color(0xFF22C55E), "Available")
                        LegendItem(Color(0xFFF59E0B), "Reserved")
                        LegendItem(Color(0xFFEF4444), "Sold (Pending)")
                        LegendItem(Color(0xFF991B1B), "Sold (Paid)")
                        LegendItem(Color(0xFF6B7280), "Blocked")
                    }
                }
            }

            // ─── TOP Banner in Drawing Mode ───────────────────────────────────
            if (state.isDrawing) {
                val bannerTitle = when (state.drawMode) {
                    DrawMode.DRAWING_LAND -> "Drawing land boundary"
                    DrawMode.DRAWING_PLOT -> "Drawing plot in ${state.selectedParentLand?.name ?: "Land"}"
                    else -> "Drawing mode"
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = DarkOverlayBg,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFF22C55E), CircleShape)
                        )
                        Text(
                            text = if (state.drawingPoints.size < 3)
                                "$bannerTitle (${state.drawingPoints.size}/3 min)"
                            else
                                "$bannerTitle — ${state.drawingPoints.size} points placed",
                            style = MaterialTheme.typography.labelMedium.copy(color = Color.White),
                        )
                    }
                }
            }

            // ─── BOTTOM-RIGHT Draw Plot / Drawing Action FABs ─────────────────
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 24.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End,
            ) {
                if (state.isDrawing) {
                    // Undo last point button
                    if (state.drawingPoints.isNotEmpty()) {
                        SmallFloatingActionButton(
                            onClick = vm::removeLastDrawingPoint,
                            containerColor = DarkOverlayBg,
                            contentColor = Color.White,
                        ) {
                            Icon(Icons.Default.Undo, contentDescription = "Undo Point")
                        }
                    }

                    // Complete Polygon button (appears after >= 3 points)
                    if (state.drawingPoints.size >= 3) {
                        ExtendedFloatingActionButton(
                            onClick = {
                                vm.onDrawComplete { boundaryJson ->
                                    onNavigateToAddLand?.invoke(boundaryJson)
                                }
                            },
                            icon = { Icon(Icons.Default.Check, contentDescription = "Complete") },
                            text = { Text("Complete") },
                            containerColor = Evergreen,
                            contentColor = Color.White,
                        )
                    }

                    // Cancel Draw Mode FAB
                    FloatingActionButton(
                        onClick = vm::cancelDrawing,
                        containerColor = Color(0xFFEF4444),
                        contentColor = Color.White,
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel Drawing")
                    }
                } else {
                    // Primary "Draw" FAB -> opens Draw Type Selector Sheet
                    ExtendedFloatingActionButton(
                        onClick = vm::onDrawFabTapped,
                        icon = { Icon(Icons.Default.Edit, contentDescription = "Draw") },
                        text = { Text("Draw") },
                        containerColor = Evergreen,
                        contentColor = Color.White,
                    )
                }
            }

            // ─── Existing Plot detail bottom sheet ────────────────────────────
            state.selectedPlot?.let { plot ->
                PlotDetailBottomCard(
                    plot = plot,
                    onClose = { vm.onPlotSelected(null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                )
            }

            // ─── CHANGE 1: Draw Type Selector Sheet ───────────────────────────
            if (state.drawMode == DrawMode.SELECTING_TYPE) {
                DrawTypeSelectorSheet(
                    onDismiss = vm::cancelDrawing,
                    onSelectLand = vm::onDrawTypeLand,
                    onSelectPlot = vm::onDrawTypePlot,
                )
            }

            // ─── CHANGE 3: Parent Land Selector Sheet ─────────────────────────
            if (state.drawMode == DrawMode.SELECTING_LAND) {
                SelectParentLandSheet(
                    lands = state.lands,
                    onDismiss = vm::cancelDrawing,
                    onSelectLand = vm::onParentLandSelected,
                    onDrawLandClicked = vm::onDrawTypeLand,
                )
            }

            // ─── Save Drawn Plot Dialog/Sheet ─────────────────────────────────
            if (state.showSavePlotSheet) {
                SavePlotDialog(
                    lands = state.lands,
                    lockedLand = state.selectedParentLand,
                    isSaving = state.isSavingPlot,
                    onDismiss = vm::closeSavePlotSheet,
                    onSave = { landId, plotNum, area, price, notes ->
                        vm.saveDrawnPlot(landId, plotNum, area, price, notes)
                    },
                    onNavigateToLand = {
                        vm.closeSavePlotSheet()
                        vm.cancelDrawing()
                        onNavigateToLand?.invoke()
                    },
                )
            }
        }
    }
}

// ─── CHANGE 1: Draw Type Selector Sheet ───────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DrawTypeSelectorSheet(
    onDismiss: () -> Unit,
    onSelectLand: () -> Unit,
    onSelectPlot: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Paper50,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "What do you want to draw?",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Soil900,
                    fontWeight = FontWeight.Bold,
                ),
            )

            // Option 1: Land Boundary
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onSelectLand),
                shape = RoundedCornerShape(14.dp),
                color = Paper100,
                border = androidx.compose.foundation.BorderStroke(1.dp, Paper200),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Evergreen50, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.Landscape, contentDescription = null, tint = Evergreen)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Land Boundary",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Soil900,
                                fontWeight = FontWeight.SemiBold,
                            ),
                        )
                        Text(
                            "Mark a new land you purchased",
                            style = MaterialTheme.typography.bodySmall.copy(color = Soil500),
                        )
                    }
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Soil300)
                }
            }

            // Option 2: Plot
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onSelectPlot),
                shape = RoundedCornerShape(14.dp),
                color = Paper100,
                border = androidx.compose.foundation.BorderStroke(1.dp, Paper200),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Evergreen50, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.GridView, contentDescription = null, tint = Evergreen)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Plot",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Soil900,
                                fontWeight = FontWeight.SemiBold,
                            ),
                        )
                        Text(
                            "Divide land into a sellable plot",
                            style = MaterialTheme.typography.bodySmall.copy(color = Soil500),
                        )
                    }
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Soil300)
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

// ─── CHANGE 3: Parent Land Selector Sheet ─────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectParentLandSheet(
    lands: List<Land>,
    onDismiss: () -> Unit,
    onSelectLand: (Land) -> Unit,
    onDrawLandClicked: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Paper50,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "Select Parent Land",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Soil900,
                    fontWeight = FontWeight.Bold,
                ),
            )

            if (lands.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(Icons.Filled.Landscape, contentDescription = null, tint = Soil300, modifier = Modifier.size(56.dp))
                    Text(
                        "Add a land boundary first",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Soil900,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                    Text(
                        "No lands created yet. Mark a land boundary first before adding plots.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Soil500,
                            textAlign = TextAlign.Center,
                        ),
                    )
                    Button(
                        onClick = onDrawLandClicked,
                        colors = ButtonDefaults.buttonColors(containerColor = Evergreen),
                    ) {
                        Text("Draw Land")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(lands, key = { it.id }) { land ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectLand(land) },
                            shape = RoundedCornerShape(12.dp),
                            color = Paper100,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Paper200),
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        land.name,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = Soil900,
                                            fontWeight = FontWeight.SemiBold,
                                        ),
                                    )
                                    Text(
                                        "${land.location} • ${land.areaAcres} acres",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Soil500),
                                    )
                                }
                                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Soil300)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

// ─── Component Helpers ────────────────────────────────────────────────────────

@Composable
private fun StatusCountItem(dotColor: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(dotColor, CircleShape)
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color.White,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}

@Composable
private fun LegendItem(dotColor: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(dotColor, CircleShape)
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(color = Color.White),
        )
    }
}

@Composable
private fun PlotDetailBottomCard(
    plot: Plot,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.padding(12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Paper50),
        elevation = CardDefaults.cardElevation(8.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
        shape = RoundedCornerShape(8.dp),
        color = Paper100,
        border = androidx.compose.foundation.BorderStroke(1.dp, Paper200),
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium.copy(color = Soil700),
        )
    }
}

@Composable
private fun StatusBadge(label: String, status: PlotStatus) {
    val (bg, fg) = when (status) {
        PlotStatus.AVAILABLE -> Evergreen50 to Evergreen
        PlotStatus.RESERVED -> Color(0xFFFFFBEB) to Amber500
        PlotStatus.SOLD_PENDING -> Color(0xFFFFF7ED) to Orange500
        PlotStatus.SOLD_PAID -> Terracotta50 to Terracotta
        PlotStatus.BLOCKED -> Color(0xFFF1F5F9) to Slate500
    }
    Surface(shape = RoundedCornerShape(8.dp), color = bg) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium.copy(color = fg, fontWeight = FontWeight.Medium),
        )
    }
}

// ─── Save Drawn Plot Dialog / Sheet ───────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SavePlotDialog(
    lands: List<Land>,
    lockedLand: Land? = null,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (landId: String, plotNumber: String, areaSqft: Double, pricePerSqft: Double?, notes: String?) -> Unit,
    onNavigateToLand: () -> Unit,
) {
    var selectedLandId by remember { mutableStateOf(lockedLand?.id ?: lands.firstOrNull()?.id ?: "") }
    var plotNumber by remember { mutableStateOf("") }
    var areaSqft by remember { mutableStateOf("") }
    var pricePerSqft by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save Drawn Plot", style = MaterialTheme.typography.titleLarge.copy(color = Soil900)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (lands.isEmpty() && lockedLand == null) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            "No lands added yet",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Soil900,
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                        Text(
                            "You need to add a land first before saving plots. Go to the Land tab to create one.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Soil500,
                                textAlign = TextAlign.Center,
                            ),
                        )
                        Button(
                            onClick = onNavigateToLand,
                            colors = ButtonDefaults.buttonColors(containerColor = Evergreen),
                        ) {
                            Text("Go to Land")
                        }
                    }
                } else {
                    Text("Parent Land", style = MaterialTheme.typography.labelMedium.copy(color = Soil700))
                    if (lockedLand != null) {
                        // Locked land selector when drawn from pre-selected parent land
                        OutlinedTextField(
                            value = "${lockedLand.name} (Locked)",
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledBorderColor = Paper200,
                                disabledTextColor = Soil900,
                                disabledLabelColor = Soil500,
                            ),
                        )
                    } else {
                        ExposedDropdownMenuBox(
                            expanded = isDropdownExpanded,
                            onExpandedChange = { isDropdownExpanded = !isDropdownExpanded },
                        ) {
                            OutlinedTextField(
                                value = lands.firstOrNull { it.id == selectedLandId }?.name ?: "Select Land",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                            )
                            ExposedDropdownMenu(
                                expanded = isDropdownExpanded,
                                onDismissRequest = { isDropdownExpanded = false },
                            ) {
                                lands.forEach { land ->
                                    DropdownMenuItem(
                                        text = { Text(land.name) },
                                        onClick = {
                                            selectedLandId = land.id
                                            isDropdownExpanded = false
                                        },
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = plotNumber,
                        onValueChange = { plotNumber = it },
                        label = { Text("Plot Number (e.g. A-05)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    OutlinedTextField(
                        value = areaSqft,
                        onValueChange = { areaSqft = it },
                        label = { Text("Area (sq.ft)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    OutlinedTextField(
                        value = pricePerSqft,
                        onValueChange = { pricePerSqft = it },
                        label = { Text("Base Price / sq.ft (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    errorMessage?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error))
                    }
                }
            }
        },
        confirmButton = {
            if (lands.isNotEmpty() || lockedLand != null) {
                Button(
                    onClick = {
                        val targetLandId = lockedLand?.id ?: selectedLandId
                        if (targetLandId.isBlank()) {
                            errorMessage = "Please select a parent land"
                            return@Button
                        }
                        if (plotNumber.isBlank()) {
                            errorMessage = "Plot number is required"
                            return@Button
                        }
                        val area = areaSqft.toDoubleOrNull()
                        if (area == null || area <= 0) {
                            errorMessage = "Enter a valid area in sq.ft"
                            return@Button
                        }
                        val price = pricePerSqft.toDoubleOrNull()
                        onSave(targetLandId, plotNumber, area, price, notes.ifBlank { null })
                    },
                    enabled = !isSaving,
                    colors = ButtonDefaults.buttonColors(containerColor = Evergreen),
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Save Plot")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text("Cancel", color = Soil500)
            }
        },
    )
}
