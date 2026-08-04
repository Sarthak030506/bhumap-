package com.bhumap.app.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.bhumap.app.data.local.db.BhumapDatabase
import com.bhumap.app.domain.model.Land
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

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
        runCatching {
            val remote = supabase.postgrest["lands"]
                .select {
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<RemoteLand>()

            println("BhumapApp LandRepository.sync(): Fetched ${remote.size} lands from Supabase")

            remote.forEach { r ->
                val areaAcres = r.totalAreaSqft?.let { it / 43560.0 } ?: r.areaAcres ?: 0.0
                val totalCost = r.agreedPrice ?: r.totalCost ?: 0.0
                val location = r.village ?: r.locationDescription ?: r.location ?: ""

                queries.upsert(
                    id          = r.id,
                    name        = r.name,
                    location    = location,
                    area_acres  = areaAcres,
                    total_cost  = totalCost,
                    notes       = r.notes,
                    created_at  = r.createdAt,
                    updated_at  = r.updatedAt,
                )
            }
        }.onFailure { e ->
            println("BhumapApp LandRepository.sync() error: ${e.message}")
        }
    }

    /**
     * LOCAL-FIRST insert: write to SQLDelight FIRST, then push to Supabase.
     * Maps domain Land fields to exact Supabase PostgreSQL lands table column names:
     *   total_area_sqft = areaAcres * 43560.0
     *   agreed_price = totalCost
     */
    suspend fun insert(land: Land) {
        // 1. Write local SQLDelight FIRST (always succeeds)
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
        println("BhumapApp LandRepository.insert(): Saved locally (id=${land.id})")

        // 2. Push to Supabase PostgREST using exact table column names from migration SQL
        val totalAreaSqft = land.areaAcres * 43560.0
        val payload = buildJsonObject {
            put("id", land.id)
            put("name", land.name)
            put("village", land.location)
            put("location_description", land.location)
            put("total_area_sqft", totalAreaSqft)
            put("agreed_price", land.totalCost)
            if (!land.notes.isNullOrBlank()) put("notes", land.notes)
            put("created_at", land.createdAt)
            put("updated_at", land.updatedAt)
        }

        runCatching {
            supabase.postgrest["lands"].insert(payload)
            println("BhumapApp LandRepository.insert(): Pushed to Supabase (id=${land.id})")
        }.onFailure { e ->
            println("BhumapApp LandRepository.insert() remote push error: ${e.message}")
        }
    }

    suspend fun update(land: Land) {
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

        runCatching {
            val totalAreaSqft = land.areaAcres * 43560.0
            supabase.postgrest["lands"].update({
                set("name", land.name)
                set("village", land.location)
                set("total_area_sqft", totalAreaSqft)
                set("agreed_price", land.totalCost)
                if (land.notes != null) set("notes", land.notes)
            }) {
                filter { eq("id", land.id) }
            }
        }
    }

    suspend fun delete(id: String) {
        queries.delete(id)
        runCatching {
            supabase.postgrest["lands"].delete {
                filter { eq("id", id) }
            }
        }
    }
}

/** Supabase DTO supporting both snake_case DB columns and fallback camelCase fields */
@Serializable
private data class RemoteLand(
    @SerialName("id")                   val id: String,
    @SerialName("name")                 val name: String,
    @SerialName("village")              val village: String? = null,
    @SerialName("location_description") val locationDescription: String? = null,
    @SerialName("location")             val location: String? = null,
    @SerialName("total_area_sqft")      val totalAreaSqft: Double? = null,
    @SerialName("area_acres")           val areaAcres: Double? = null,
    @SerialName("agreed_price")         val agreedPrice: Double? = null,
    @SerialName("total_cost")           val totalCost: Double? = null,
    @SerialName("notes")                val notes: String? = null,
    @SerialName("created_at")           val createdAt: String,
    @SerialName("updated_at")           val updatedAt: String,
)
