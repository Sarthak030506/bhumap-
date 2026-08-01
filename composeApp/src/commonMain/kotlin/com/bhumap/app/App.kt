package com.bhumap.app

import androidx.compose.runtime.Composable
import com.bhumap.app.ui.navigation.AppNavHost
import com.bhumap.app.ui.theme.BhumapTheme

/**
 * Root composable — shared entry point called by both:
 *   - Android: MainActivity.setContent { App() }
 *   - iOS: MainViewController → ComposeUIViewController { App() }
 */
@Composable
fun App() {
    BhumapTheme {
        AppNavHost()
    }
}
