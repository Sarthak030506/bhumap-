package com.bhumap.app.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.bhumap.app.data.local.db.BhumapDatabase
import com.bhumap.app.domain.model.Partner
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class PartnerRepository(
    private val db: BhumapDatabase,
    private val supabase: SupabaseClient,
) {
    private val queries get() = db.partnerQueries

    /** Observe partners by landId from local cache */
    fun observeByLandId(landId: String): Flow<List<Partner>> =
        queries.selectByLand(landId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows ->
                rows.map { row ->
                    Partner(
                        id              = row.id,
                        landId          = row.land_id,
                        name            = row.name,
                        phone           = row.phone,
                        committedAmount = row.committed_amount,
                        paidAmount      = row.paid_amount,
                        profitSharePct  = row.profit_share_pct,
                        notes           = row.notes,
                        createdAt       = row.created_at,
                        updatedAt       = row.updated_at,
                    )
                }
            }

    /** Fetch partners from Supabase and upsert into local SQLDelight DB */
    suspend fun sync() {
        runCatching {
            val remote = supabase.postgrest["partners"]
                .select()
                .decodeList<RemotePartner>()

            println("BhumapApp PartnerRepository.sync(): Fetched ${remote.size} partners from Supabase")

            remote.forEach { p ->
                queries.upsert(
                    id               = p.id,
                    land_id          = p.landId,
                    name             = p.name,
                    phone            = p.phone,
                    committed_amount = p.committedAmount,
                    paid_amount      = p.paidAmount,
                    profit_share_pct = p.profitSharePct,
                    notes            = p.notes,
                    created_at       = p.createdAt,
                    updated_at       = p.updatedAt,
                )
            }
        }.onFailure { err ->
            println("BhumapApp PartnerRepository.sync() error: ${err.message}")
        }
    }

    /**
     * LOCAL-FIRST insert: write to SQLDelight FIRST, then push to Supabase.
     */
    suspend fun insert(partner: Partner) {
        // 1. Write local
        queries.upsert(
            id               = partner.id,
            land_id          = partner.landId,
            name             = partner.name,
            phone            = partner.phone,
            committed_amount = partner.committedAmount,
            paid_amount      = partner.paidAmount,
            profit_share_pct = partner.profitSharePct,
            notes            = partner.notes,
            created_at       = partner.createdAt,
            updated_at       = partner.updatedAt,
        )
        println("BhumapApp PartnerRepository.insert(): Saved locally (id=${partner.id})")

        // 2. Push remote
        val remoteDto = RemotePartner(
            id              = partner.id,
            landId          = partner.landId,
            name            = partner.name,
            phone           = partner.phone,
            committedAmount = partner.committedAmount,
            paidAmount      = partner.paidAmount,
            profitSharePct  = partner.profitSharePct,
            notes           = partner.notes,
            createdAt       = partner.createdAt,
            updatedAt       = partner.updatedAt,
        )
        supabase.postgrest["partners"].insert(remoteDto)
        println("BhumapApp PartnerRepository.insert(): Pushed to Supabase (id=${partner.id})")
    }

    /** Delete partner locally first, then push delete to Supabase */
    suspend fun delete(id: String) {
        queries.delete(id)
        runCatching {
            supabase.postgrest["partners"].delete {
                filter { eq("id", id) }
            }
        }
    }
}

@Serializable
private data class RemotePartner(
    @SerialName("id")               val id: String,
    @SerialName("land_id")          val landId: String,
    @SerialName("name")             val name: String,
    @SerialName("phone")            val phone: String,
    @SerialName("committed_amount") val committedAmount: Double = 0.0,
    @SerialName("paid_amount")      val paidAmount: Double = 0.0,
    @SerialName("profit_share_pct") val profitSharePct: Double = 0.0,
    @SerialName("notes")            val notes: String? = null,
    @SerialName("created_at")       val createdAt: String,
    @SerialName("updated_at")       val updatedAt: String,
)
