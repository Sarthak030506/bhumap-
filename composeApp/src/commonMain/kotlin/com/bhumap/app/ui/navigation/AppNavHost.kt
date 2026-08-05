package com.bhumap.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.navArgument
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
import io.github.jan.supabase.auth.status.SessionStatus
import org.koin.compose.koinInject

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val authRepo      = koinInject<AuthRepository>()

    // ── Session gate: prevents auth-screen flash on cold start ───────────────
    // Supabase restores the persisted session asynchronously. sessionStatusFlow
    // starts as LoadingFromStorage. We show a blank Box until it settles so the
    // login screen never flickers in when a valid session already exists.
    val sessionStatus by authRepo.sessionStatusFlow.collectAsState()

    // React to session changes (e.g. sign-in, sign-out, token refresh)
    LaunchedEffect(sessionStatus) {
        when (sessionStatus) {
            is SessionStatus.Authenticated -> {
                // If we're on an auth screen after session restore, go to Dashboard
                val currentRoute = navController.currentBackStackEntry?.destination?.route
                if (currentRoute == Screen.Login.route ||
                    currentRoute?.startsWith("otp") == true) {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            is SessionStatus.NotAuthenticated -> {
                // If we're on a protected screen (e.g. after sign-out), send to Login
                val currentRoute = navController.currentBackStackEntry?.destination?.route
                if (currentRoute != null &&
                    currentRoute != Screen.Login.route &&
                    !currentRoute.startsWith("otp")) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            else -> { /* Initializing or RefreshFailure — stay put */ }
        }
    }

    // Blank screen while session is still loading — no auth flash
    if (sessionStatus is SessionStatus.Initializing) {
        Box(modifier = Modifier.fillMaxSize())
        return
    }

    // Once session is known, pick the correct start destination synchronously
    val startDest = if (sessionStatus is SessionStatus.Authenticated)
        Screen.Dashboard.route else Screen.Login.route

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
            composable(Screen.LandList.route) {
                LandListScreen(
                    onAdd    = { navController.navigate(Screen.AddLand.route) },
                    onSelect = { id -> navController.navigate(Screen.LandDetail.build(id)) },
                )
            }
            composable(Screen.Map.route) {
                MapScreen(
                    onNavigateToLand = { navController.navigate(Screen.LandList.route) },
                    onNavigateToAddLand = { boundary ->
                        navController.navigate(Screen.AddLand.build(boundary))
                    },
                )
            }
            composable(Screen.Customers.route) {
                CustomerListScreen(
                    onSelect = { id -> navController.navigate(Screen.CustomerDetail.build(id)) }
                )
            }

            // ─── Land detail ──────────────────────────────────────────────────
            composable(
                route = "land/add?boundary={boundary}",
                arguments = listOf(
                    navArgument("boundary") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { back ->
                val boundary = back.arguments?.getString("boundary")
                AddLandScreen(
                    boundaryJson = boundary,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Screen.LandDetail.route) { back ->
                val landId = back.arguments?.getString("landId") ?: ""
                LandDetailScreen(landId = landId, onBack = { navController.popBackStack() })
            }

            // ─── Customer detail ──────────────────────────────────────────────
            composable(Screen.CustomerDetail.route) { back ->
                val customerId = back.arguments?.getString("customerId") ?: ""
                CustomerDetailScreen(customerId = customerId, onBack = { navController.popBackStack() })
            }
        }
    }
}
