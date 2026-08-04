package com.bhumap.app.ui.land

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bhumap.app.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLandScreen(
    boundaryJson: String? = null,
    onBack: () -> Unit,
) {
    val vm: LandViewModel = koinViewModel()
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add land") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Paper50),
            )
        },
        containerColor = Paper50,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ─── Boundary Marked Confirmation Chip ───────────────────────────
            if (!boundaryJson.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Evergreen50,
                    border = BorderStroke(1.dp, Evergreen.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = Evergreen)
                        Text(
                            "✓ Boundary marked on map",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Evergreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }

            BhuTextField(
                value         = state.formName,
                onValueChange = vm::onNameChange,
                label         = "Land name *",
                placeholder   = "e.g. Pawar Farm, Nashik Road",
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            )
            BhuTextField(
                value         = state.formLocation,
                onValueChange = vm::onLocationChange,
                label         = "Location / Village *",
                placeholder   = "e.g. Sinner, Nashik",
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BhuTextField(
                    modifier      = Modifier.weight(1f),
                    value         = state.formArea,
                    onValueChange = vm::onAreaChange,
                    label         = "Area (acres) *",
                    placeholder   = "e.g. 5.5",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    suffix        = "acres",
                )
                BhuTextField(
                    modifier      = Modifier.weight(1f),
                    value         = state.formCost,
                    onValueChange = vm::onCostChange,
                    label         = "Total cost (₹) *",
                    placeholder   = "e.g. 2500000",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    prefix        = "₹",
                )
            }
            BhuTextField(
                value         = state.formNotes,
                onValueChange = vm::onNotesChange,
                label         = "Notes",
                placeholder   = "Any additional details…",
                singleLine    = false,
                minLines      = 3,
            )

            if (state.saveError != null) {
                Text(
                    state.saveError!!,
                    style = MaterialTheme.typography.bodySmall.copy(color = Terracotta),
                )
            }

            Spacer(Modifier.height(8.dp))

            // ─── Sticky save button ────────────────────────────────────────────
            Button(
                onClick  = { vm.saveLand(boundaryJson, onBack) },
                enabled  = vm.isFormValid && !state.isSaving,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Evergreen),
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(Modifier.size(22.dp), color = Paper50, strokeWidth = 2.dp)
                } else {
                    Text("Save land", style = MaterialTheme.typography.labelLarge.copy(color = Paper50))
                }
            }
        }
    }
}

// Reusable field used only in this module (global BhuTextField is in components)
@Composable
private fun BhuTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    singleLine: Boolean = true,
    minLines: Int = 1,
    prefix: String? = null,
    suffix: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        modifier      = modifier.fillMaxWidth(),
        label         = { Text(label) },
        placeholder   = { Text(placeholder, color = Soil300) },
        singleLine    = singleLine,
        minLines      = minLines,
        prefix        = prefix?.let { { Text(it, color = Soil500) } },
        suffix        = suffix?.let { { Text(it, color = Soil500) } },
        keyboardOptions = keyboardOptions,
        shape         = RoundedCornerShape(12.dp),
        colors        = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Evergreen,
            focusedLabelColor  = Evergreen,
            cursorColor        = Evergreen,
        ),
    )
}
