package com.bhumap.app.data.local

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import app.cash.sqldelight.db.SqlDriver
import com.bhumap.app.data.local.db.BhumapDatabase

actual class DatabaseDriverFactory actual constructor(context: Any?) {
    private val androidContext = context as Context
    actual fun createDriver(): SqlDriver =
        AndroidSqliteDriver(BhumapDatabase.Schema, androidContext, "bhumap.db")
}
