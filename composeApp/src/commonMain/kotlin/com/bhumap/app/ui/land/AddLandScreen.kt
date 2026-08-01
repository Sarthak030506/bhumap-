package com.bhumap.app.ui.land

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bhumap.app.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLandScreen(onBack: () -> Unit) {
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
