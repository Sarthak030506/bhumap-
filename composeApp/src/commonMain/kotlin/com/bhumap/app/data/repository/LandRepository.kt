package com.bhumap.app.data.repository

import com.bhumap.app.data.local.db.BhumapDatabase
import com.bhumap.app.domain.model.Land
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

class LandRepository(
    private val db: BhumapDatabase,
