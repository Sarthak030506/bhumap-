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
    private val supabase: SupabaseClient,
) {
    private val queries get() = db.landQueries

    /** Observe local cache — emits immediately, then on every DB change */
    fun observeAll(): Flow<List<com.bhumap.app.data.local.db.Land>> =
        queries.selectAll().asFlow().mapToList(Dispatchers.IO)

    /** Fetch from Supabase and upsert into local DB */
    suspend fun sync() {
        val remote = supabase.postgrest["lands"]
            .select {
                order("created_at", Order.DESCENDING)
            }
            .decodeList<Land>()

