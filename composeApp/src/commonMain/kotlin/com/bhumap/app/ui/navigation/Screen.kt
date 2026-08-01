package com.bhumap.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.People
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    // ─── Auth ─────────────────────────────────────────────────────────────────
    data object Login  : Screen("login")
    data object Otp    : Screen("otp/{phone}") {
        fun build(phone: String) = "otp/$phone"
    }

    // ─── Main Tabs ────────────────────────────────────────────────────────────
    data object Dashboard : Screen("dashboard")
    data object LandList  : Screen("land")
    data object Map       : Screen("map")
    data object Customers : Screen("customers")

    // ─── Land sub-screens ─────────────────────────────────────────────────────
    data object AddLand    : Screen("land/add")
    data object LandDetail : Screen("land/{landId}") {
        fun build(landId: String) = "land/$landId"
    }

    // ─── Customer sub-screens ─────────────────────────────────────────────────
    data object CustomerDetail : Screen("customers/{customerId}") {
        fun build(customerId: String) = "customers/$customerId"
    }
}

/** Bottom navigation tab descriptors */
data class BottomTab(
