package com.bhumap.app.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.bhumap.app.data.local.db.BhumapDatabase
import com.bhumap.app.domain.model.EntityType
import com.bhumap.app.domain.model.PaymentMode
import com.bhumap.app.domain.model.Transaction
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class TransactionRepository(
    private val db: BhumapDatabase,
    private val supabase: SupabaseClient,
) {
    private val queries get() = db.transactionQueries

    /** Observe local transactions by saleId */
    fun observeBySale(saleId: String): Flow<List<Transaction>> =
        queries.selectBySale(saleId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows ->
                rows.map { row ->
                    Transaction(
                        id          = row.id,
                        entityType  = parseEntityType(row.entity_type),
                        entityId    = row.entity_id,
                        saleId      = row.sale_id,
                        amount      = row.amount,
                        paymentMode = parsePaymentMode(row.payment_mode),
                        referenceNo = row.reference_no,
                        paymentDate = row.payment_date,
                        notes       = row.notes,
                        createdAt   = row.created_at,
                    )
                }
            }

    /** Observe local transactions by entity (farmer / partner / customer) */
    fun observeByEntity(entityType: EntityType, entityId: String): Flow<List<Transaction>> =
        queries.selectByEntity(entityType.name, entityId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows ->
                rows.map { row ->
                    Transaction(
                        id          = row.id,
                        entityType  = parseEntityType(row.entity_type),
                        entityId    = row.entity_id,
                        saleId      = row.sale_id,
                        amount      = row.amount,
                        paymentMode = parsePaymentMode(row.payment_mode),
                        referenceNo = row.reference_no,
                        paymentDate = row.payment_date,
                        notes       = row.notes,
                        createdAt   = row.created_at,
                    )
                }
            }

    /** Pull fresh transactions from Supabase and upsert to local DB */
    suspend fun sync() {
        runCatching {
            val remote = supabase.postgrest["transactions"]
                .select()
                .decodeList<RemoteTransaction>()

            println("BhumapApp TransactionRepository.sync(): Fetched ${remote.size} transactions from Supabase")

            remote.forEach { t ->
                queries.insert(
                    id           = t.id,
                    entity_type  = t.entityType,
                    entity_id    = t.entityId,
                    sale_id      = t.saleId,
                    amount       = t.amount,
                    payment_mode = t.paymentMode,
                    reference_no = t.referenceNo,
                    payment_date = t.paymentDate,
                    notes        = t.notes,
                    created_at   = t.createdAt,
                )
            }
        }.onFailure { e ->
            println("BhumapApp TransactionRepository.sync() error: ${e.message}")
        }
    }

    /**
     * LOCAL-FIRST insert: write to SQLDelight FIRST, then push to Supabase.
     */
    suspend fun insert(txn: Transaction) {
        // 1. Write local
        queries.insert(
            id           = txn.id,
            entity_type  = txn.entityType.name,
            entity_id    = txn.entityId,
            sale_id      = txn.saleId,
            amount       = txn.amount,
            payment_mode = txn.paymentMode.name,
            reference_no = txn.referenceNo,
            payment_date = txn.paymentDate,
            notes        = txn.notes,
            created_at   = txn.createdAt,
        )
        println("BhumapApp TransactionRepository.insert(): Saved locally (id=${txn.id})")

        // 2. Push remote
        val remoteDto = RemoteTransaction(
            id          = txn.id,
            entityType  = txn.entityType.name,
            entityId    = txn.entityId,
            saleId      = txn.saleId,
            amount      = txn.amount,
            paymentMode = txn.paymentMode.name,
            referenceNo = txn.referenceNo,
            paymentDate = txn.paymentDate,
            notes       = txn.notes,
            createdAt   = txn.createdAt,
        )
        supabase.postgrest["transactions"].insert(remoteDto)
        println("BhumapApp TransactionRepository.insert(): Pushed to Supabase (id=${txn.id})")
    }
}

private fun parseEntityType(type: String): EntityType = runCatching {
    EntityType.valueOf(type.uppercase())
}.getOrDefault(EntityType.CUSTOMER)

private fun parsePaymentMode(mode: String): PaymentMode = runCatching {
    PaymentMode.valueOf(mode.uppercase())
}.getOrDefault(PaymentMode.CASH)

@Serializable
private data class RemoteTransaction(
    @SerialName("id")            val id: String,
    @SerialName("entity_type")   val entityType: String = "CUSTOMER",
    @SerialName("entity_id")     val entityId: String = "",
    @SerialName("sale_id")       val saleId: String? = null,
    @SerialName("amount")        val amount: Double,
    @SerialName("payment_mode")  val paymentMode: String = "CASH",
    @SerialName("reference_no")  val referenceNo: String? = null,
    @SerialName("payment_date")  val paymentDate: String,
    @SerialName("notes")         val notes: String? = null,
    @SerialName("created_at")    val createdAt: String,
)
