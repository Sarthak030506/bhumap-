package com.bhumap.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bhumap.app.data.repository.AuthRepository
import com.bhumap.app.ui.auth.LoginScreen
import com.bhumap.app.ui.auth.OtpScreen
import com.bhumap.app.ui.customers.CustomerDetailScreen
import com.bhumap.app.ui.customers.CustomerListScreen
import com.bhumap.app.ui.dashboard.DashboardScreen
import com.bhumap.app.ui.land.AddLandScreen
import com.bhumap.app.ui.land.LandDetailScreen
import com.bhumap.app.ui.land.LandListScreen
import com.bhumap.app.ui.map.MapScreen
import org.koin.compose.koinInject

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val authRepo      = koinInject<AuthRepository>()
    val startDest     = if (authRepo.isLoggedIn) Screen.Dashboard.route else Screen.Login.route

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = bottomTabs.any { tab ->
        currentDestination?.hierarchy?.any { it.route == tab.screen.route } == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                ) {
                    bottomTabs.forEach { tab ->
                        val selected = currentDestination?.hierarchy
                            ?.any { it.route == tab.screen.route } == true

                        NavigationBarItem(
                            selected = selected,
                            onClick  = {
                                navController.navigate(tab.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            },
                            icon  = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor   = MaterialTheme.colorScheme.primary,
                                selectedTextColor   = MaterialTheme.colorScheme.primary,
                                indicatorColor      = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = startDest,
            modifier         = Modifier.padding(innerPadding),
        ) {
            // ─── Auth ──────────────────────────────────────────────────────────
            composable(Screen.Login.route) {
                LoginScreen(
                    onOtpSent = { phone ->
                        navController.navigate(Screen.Otp.build(phone))
                    }
                )
            }
            composable(Screen.Otp.route) { back ->
                val phone = back.arguments?.getString("phone") ?: ""
                OtpScreen(
                    phone     = phone,
                    onSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                )
            }

            // ─── Main tabs ────────────────────────────────────────────────────
            composable(Screen.Dashboard.route) { DashboardScreen() }
