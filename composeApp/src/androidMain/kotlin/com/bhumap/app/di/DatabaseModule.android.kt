package com.bhumap.app.di

import com.bhumap.app.data.local.DatabaseDriverFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.scope.Scope

actual fun Scope.createDatabaseDriverFactory(): DatabaseDriverFactory {
    return DatabaseDriverFactory(androidContext())
}
