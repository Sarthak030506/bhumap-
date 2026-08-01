package com.bhumap.app

import androidx.compose.ui.window.ComposeUIViewController
import com.bhumap.app.di.appModule
import com.bhumap.app.di.databaseModule
import com.bhumap.app.di.networkModule
import org.koin.core.context.startKoin

/**
 * iOS entry point called by ContentView.swift via:
 *   MainViewControllerKt.MainViewController()
 */
fun MainViewController() = ComposeUIViewController(
    configure = {
        startKoin {
