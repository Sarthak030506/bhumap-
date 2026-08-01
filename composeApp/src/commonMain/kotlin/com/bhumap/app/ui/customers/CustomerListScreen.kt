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
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // ─── Search bar ───────────────────────────────────────────────────
            OutlinedTextField(
                value         = state.searchQuery,
                onValueChange = vm::onSearchChange,
                modifier      = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder   = { Text("Search name or phone") },
                leadingIcon   = { Icon(Icons.Filled.Search, null, tint = Soil500) },
                shape         = RoundedCornerShape(12.dp),
                singleLine    = true,
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Evergreen,
                    cursorColor        = Evergreen,
                ),
            )

            when {
                state.isLoading -> CustomerSkeleton()
                filtered.isEmpty() -> CustomerEmptyState()
                else -> LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp, top = 4.dp, end = 16.dp, bottom = 80.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(filtered, key = { it.id }) { customer ->
                        CustomerRow(customer = customer, onClick = { onSelect(customer.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerRow(customer: Customer, onClick: () -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = Paper50),
        elevation = CardDefaults.cardElevation(2.dp),
        border    = androidx.compose.foundation.BorderStroke(1.dp, Paper200),
    ) {
        Row(
            modifier          = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Avatar circle with initials
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Evergreen50),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    customer.name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Evergreen, fontWeight = FontWeight.Bold,
                    ),
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    customer.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Soil900, fontWeight = FontWeight.SemiBold,
                    ),
                )
                Text(
                    customer.phone,
                    style = MaterialTheme.typography.bodySmall.copy(color = Soil500),
                )
            }
            Icon(Icons.Filled.ChevronRight, null, tint = Soil300)
        }
    }
}

@Composable
private fun CustomerEmptyState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Filled.People, null, tint = Soil300, modifier = Modifier.size(72.dp))
            Spacer(Modifier.height(12.dp))
            Text("No customers yet", style = MaterialTheme.typography.titleMedium.copy(color = Soil700))
        }
    }
}

@Composable
private fun CustomerSkeleton() {
    LazyColumn(
        contentPadding      = PaddingValues(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(5) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Paper200)
            )
        }
    }
}
