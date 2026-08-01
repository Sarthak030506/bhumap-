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

        remote.forEach { land ->
            queries.upsert(
                id          = land.id,
                name        = land.name,
                location    = land.location,
                area_acres  = land.areaAcres,
                total_cost  = land.totalCost,
                notes       = land.notes,
                created_at  = land.createdAt,
                updated_at  = land.updatedAt,
            )
        }
    }

    suspend fun insert(land: Land) {
        supabase.postgrest["lands"].insert(land)
        sync()
    }

    suspend fun update(land: Land) {
        supabase.postgrest["lands"].update(land) {
            filter { eq("id", land.id) }
        }
        sync()
    }

    suspend fun delete(id: String) {
        supabase.postgrest["lands"].delete {
            filter { eq("id", id) }
        }
        queries.delete(id)
    }
}
