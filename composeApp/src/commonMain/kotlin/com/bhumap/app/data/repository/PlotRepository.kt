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
import kotlinx.serialization.json.double
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
     * boundary_coordinates arrives as [{lat,lng},...] jsonb — we convert
     * it to [[lng,lat],...] TEXT format expected by PlatformMapView parser.
     */
    suspend fun sync() {
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
                boundary_json  = r.boundaryCoordinates?.let(::convertBoundaryToJson),
                price_per_sqft = r.basePricePerSqft,
                notes          = r.notes,
                created_at     = r.createdAt,
                updated_at     = r.updatedAt,
            )
        }
    }

    /**
     * Convert Supabase jsonb [{lat, lng}, ...] to the [[lng,lat],...] TEXT
     * format that parseBoundaryJson() in PlatformMapView.kt expects.
     * Returns null if the JSON is blank, unparseable, or has fewer than 3 points.
     */
    private fun convertBoundaryToJson(raw: String): String? {
        return try {
            val arr = Json.parseToJsonElement(raw).jsonArray
            if (arr.size < 3) return null
            val pairs = arr.joinToString(",") { element ->
                val obj = element.jsonObject
                val lat = obj["lat"]!!.jsonPrimitive.double
                val lng = obj["lng"]!!.jsonPrimitive.double
                "[$lng,$lat]"
            }
            "[$pairs]"
        } catch (_: Exception) {
            null
        }
    }
}

// ─── Supabase DTO ─────────────────────────────────────────────────────────────
// Field names must match Supabase postgres column names exactly.

@Serializable
private data class RemotePlot(
    @SerialName("id")                    val id: String,
    @SerialName("land_id")               val landId: String,
    @SerialName("plot_number")           val plotNumber: String,
    @SerialName("area_sqft")             val areaSqft: Double,
    @SerialName("status")                val status: String,
    @SerialName("boundary_coordinates")  val boundaryCoordinates: String? = null,
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
    else           -> PlotStatus.AVAILABLE   // safe fallback for unknown values
}
