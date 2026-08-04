package com.bhumap.app.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.bhumap.app.data.local.db.BhumapDatabase
import com.bhumap.app.domain.model.Plot
import com.bhumap.app.domain.model.PlotStatus
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class PlotRepository(
    private val db: BhumapDatabase,
    private val supabase: SupabaseClient,
) {
    private val queries get() = db.plotQueries

    /**
     * Observe plots that have boundary data — emits immediately from local cache,
     * then re-emits on every SQLDelight DB write (reactive flow).
     * Used by MapViewModel to draw polygons.
     */
    fun getAllPlotsWithBoundaries(): Flow<List<Plot>> =
        queries.selectAllWithBoundary()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows ->
                rows.mapNotNull { row ->
                    runCatching {
                        Plot(
                            id           = row.id,
                            landId       = row.land_id,
                            plotNumber   = row.plot_number,
                            areaSqft     = row.area_sqft,
                            status       = plotStatusFromDbValue(row.status),
                            boundaryJson = row.boundary_json,
                            pricePerSqft = row.price_per_sqft,
                            notes        = row.notes,
                            createdAt    = row.created_at,
                            updatedAt    = row.updated_at,
                        )
                    }.getOrNull()
                }
            }

    /**
     * Pull all plots from Supabase and upsert into local SQLDelight DB.
     * Supabase column names differ from Kotlin camelCase fields, so we use
     * a [RemotePlot] DTO with explicit @SerialName annotations.
     * boundary_coordinates arrives as JsonElement (jsonb) — converted safely to [[lng,lat],...] TEXT.
     */
    suspend fun sync() {
        runCatching {
            val remote = supabase.postgrest["plots"]
                .select()
                .decodeList<RemotePlot>()

            remote.forEach { r ->
                queries.upsert(
                    id             = r.id,
                    land_id        = r.landId,
                    plot_number    = r.plotNumber,
                    area_sqft      = r.areaSqft,
                    status         = r.status,
                    boundary_json  = convertBoundaryToJson(r.boundaryCoordinates),
                    price_per_sqft = r.basePricePerSqft,
                    notes          = r.notes,
                    created_at     = r.createdAt,
                    updated_at     = r.updatedAt,
                )
            }
        }.onFailure { e ->
            println("BhumapApp: PlotRepository.sync() failed: ${e.message}")
        }
    }

    /**
     * Convert Supabase jsonb element (object array, number array, or JSON string)
     * to the [[lng,lat],...] TEXT format expected by PlatformMapView parser.
     * Returns null if unparseable or has fewer than 3 points.
     */
    private fun convertBoundaryToJson(element: JsonElement?): String? {
        if (element == null) return null
        return try {
            val jsonArray = when {
                element is JsonPrimitive && element.isString -> Json.parseToJsonElement(element.content).jsonArray
                element is JsonArray -> element
                else -> return null
            }

            if (jsonArray.size < 3) return null

            val pairs = jsonArray.mapNotNull { item ->
                when {
                    item is JsonObject -> {
                        val lat = item["lat"]?.jsonPrimitive?.doubleOrNull
                            ?: item["latitude"]?.jsonPrimitive?.doubleOrNull
                        val lng = item["lng"]?.jsonPrimitive?.doubleOrNull
                            ?: item["longitude"]?.jsonPrimitive?.doubleOrNull
                        if (lat != null && lng != null) "[$lng,$lat]" else null
                    }
                    item is JsonArray && item.size >= 2 -> {
                        val v1 = item[0].jsonPrimitive.double
                        val v2 = item[1].jsonPrimitive.double
                        "[$v1,$v2]"
                    }
                    else -> null
                }
            }

            if (pairs.size < 3) null else "[${pairs.joinToString(",")}]"
        } catch (_: Exception) {
            null
        }
    }
}

// ─── Supabase DTO ─────────────────────────────────────────────────────────────

@Serializable
private data class RemotePlot(
    @SerialName("id")                    val id: String,
    @SerialName("land_id")               val landId: String,
    @SerialName("plot_number")           val plotNumber: String,
    @SerialName("area_sqft")             val areaSqft: Double,
    @SerialName("status")                val status: String,
    @SerialName("boundary_coordinates")  val boundaryCoordinates: JsonElement? = null,
    @SerialName("base_price_per_sqft")   val basePricePerSqft: Double? = null,
    @SerialName("notes")                 val notes: String? = null,
    @SerialName("created_at")            val createdAt: String,
    @SerialName("updated_at")            val updatedAt: String,
)

// ─── PlotStatus DB mapping ────────────────────────────────────────────────────

private fun plotStatusFromDbValue(value: String): PlotStatus = when (value) {
    "available"    -> PlotStatus.AVAILABLE
    "reserved"     -> PlotStatus.RESERVED
    "sold_pending" -> PlotStatus.SOLD_PENDING
    "sold_paid"    -> PlotStatus.SOLD_PAID
    "blocked"      -> PlotStatus.BLOCKED
    else           -> PlotStatus.AVAILABLE
}
