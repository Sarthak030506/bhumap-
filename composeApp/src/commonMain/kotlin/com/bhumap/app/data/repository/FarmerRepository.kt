package com.bhumap.app.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.bhumap.app.data.local.db.BhumapDatabase
import com.bhumap.app.domain.model.Farmer
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class FarmerRepository(
    private val db: BhumapDatabase,
    private val supabase: SupabaseClient,
) {
    private val queries get() = db.farmerQueries

    /** Observe farmers by landId from local cache */
    fun observeByLandId(landId: String): Flow<List<Farmer>> =
        queries.selectByLand(landId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows ->
                rows.map { row ->
                    Farmer(
                        id          = row.id,
                        landId      = row.land_id,
                        name        = row.name,
                        phone       = row.phone,
                        aadhaar     = row.aadhaar,
                        totalAgreed = row.total_agreed,
                        totalPaid   = row.total_paid,
                        createdAt   = row.created_at,
                        updatedAt   = row.updated_at,
                    )
                }
            }

    /** Fetch farmers from Supabase and upsert into local SQLDelight DB */
    suspend fun sync() {
        runCatching {
            val remote = supabase.postgrest["farmers"]
                .select()
                .decodeList<RemoteFarmer>()

            println("BhumapApp FarmerRepository.sync(): Fetched ${remote.size} farmers from Supabase")

            remote.forEach { f ->
                queries.upsert(
                    id           = f.id,
                    land_id      = f.landId,
                    name         = f.name,
                    phone        = f.phone,
                    aadhaar      = f.aadhaar,
                    total_agreed = f.totalAgreed,
                    total_paid   = f.totalPaid,
                    created_at   = f.createdAt,
                    updated_at   = f.updatedAt,
                )
            }
        }.onFailure { err ->
            println("BhumapApp FarmerRepository.sync() error: ${err.message}")
        }
    }

    /**
     * LOCAL-FIRST insert: write to SQLDelight FIRST, then push to Supabase.
     */
    suspend fun insert(farmer: Farmer) {
        // 1. Write local
        queries.upsert(
            id           = farmer.id,
            land_id      = farmer.landId,
            name         = farmer.name,
            phone        = farmer.phone,
            aadhaar      = farmer.aadhaar,
            total_agreed = farmer.totalAgreed,
            total_paid   = farmer.totalPaid,
            created_at   = farmer.createdAt,
            updated_at   = farmer.updatedAt,
        )
        println("BhumapApp FarmerRepository.insert(): Saved locally (id=${farmer.id})")

        // 2. Push remote
        val remoteDto = RemoteFarmer(
            id          = farmer.id,
            landId      = farmer.landId,
            name        = farmer.name,
            phone       = farmer.phone,
            aadhaar     = farmer.aadhaar,
            totalAgreed = farmer.totalAgreed,
            totalPaid   = farmer.totalPaid,
            createdAt   = farmer.createdAt,
            updatedAt   = farmer.updatedAt,
        )
        supabase.postgrest["farmers"].insert(remoteDto)
        println("BhumapApp FarmerRepository.insert(): Pushed to Supabase (id=${farmer.id})")
    }

    /** Delete farmer locally first, then push delete to Supabase */
    suspend fun delete(id: String) {
        queries.delete(id)
        runCatching {
            supabase.postgrest["farmers"].delete {
                filter { eq("id", id) }
            }
        }
    }
}

@Serializable
private data class RemoteFarmer(
    @SerialName("id")           val id: String,
    @SerialName("land_id")      val landId: String,
    @SerialName("name")         val name: String,
    @SerialName("phone")        val phone: String,
    @SerialName("aadhaar")      val aadhaar: String? = null,
    @SerialName("total_agreed") val totalAgreed: Double = 0.0,
    @SerialName("total_paid")   val totalPaid: Double = 0.0,
    @SerialName("created_at")   val createdAt: String,
    @SerialName("updated_at")   val updatedAt: String,
)
