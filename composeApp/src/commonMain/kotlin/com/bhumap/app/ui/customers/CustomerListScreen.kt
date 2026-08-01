package com.bhumap.app.ui.customers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bhumap.app.data.local.db.Customer
import com.bhumap.app.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListScreen(onSelect: (String) -> Unit) {
    val vm: CustomerViewModel = koinViewModel()
    val state    by vm.state.collectAsState()
    val filtered by vm.filtered.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title  = { Text("Customers") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Paper50),
            )
        },
        containerColor = Paper50,
