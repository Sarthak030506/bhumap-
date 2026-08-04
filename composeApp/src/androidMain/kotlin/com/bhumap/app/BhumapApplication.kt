package com.bhumap.app

import android.app.Application
import android.preference.PreferenceManager
import com.bhumap.app.di.appModule
import com.bhumap.app.di.databaseModule
import com.bhumap.app.di.networkModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.osmdroid.config.Configuration
import java.io.File

class BhumapApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // ─── Initialize osmdroid Global Configuration ─────────────────────────
        // Load default shared preferences BEFORE setting custom User-Agent and cache paths
        Configuration.getInstance().apply {
            load(this@BhumapApplication, PreferenceManager.getDefaultSharedPreferences(this@BhumapApplication))
            userAgentValue = "BhuMap/1.0 (Android)"
            osmdroidBasePath = File(cacheDir, "osmdroid")
            osmdroidTileCache = File(cacheDir, "osmdroid/tiles")
        }

        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@BhumapApplication)
            modules(
                appModule,
                databaseModule,
                networkModule,
            )
        }
    }
}
