package com.bhumap.app.data.local

import app.cash.sqldelight.db.SqlDriver

/**
 * Platform-specific SQLite driver factory.
 * Android → AndroidSqliteDriver
 * iOS     → NativeSqliteDriver
