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

