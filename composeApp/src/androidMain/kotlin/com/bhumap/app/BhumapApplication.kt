package com.bhumap.app

import android.app.Application
import com.bhumap.app.di.appModule
import com.bhumap.app.di.databaseModule
import com.bhumap.app.di.networkModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class BhumapApplication : Application() {
    override fun onCreate() {
        super.onCreate()
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
