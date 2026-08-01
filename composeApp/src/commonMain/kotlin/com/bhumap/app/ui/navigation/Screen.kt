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
