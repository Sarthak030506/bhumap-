package com.bhumap.app.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.bhumap.app.data.local.db.BhumapDatabase

actual class DatabaseDriverFactory actual constructor(context: Any?) {
    actual fun createDriver(): SqlDriver =
        NativeSqliteDriver(BhumapDatabase.Schema, "bhumap.db")
}
