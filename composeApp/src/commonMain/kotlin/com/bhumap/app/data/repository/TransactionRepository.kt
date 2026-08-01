package com.bhumap.app.data.repository

import com.bhumap.app.data.local.db.BhumapDatabase
import com.bhumap.app.domain.model.Transaction
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

class TransactionRepository(
    private val db: BhumapDatabase,
    private val supabase: SupabaseClient,
) {
    private val queries get() = db.transactionQueries

    fun observeBySale(saleId: String) = queries.selectBySale(saleId).asFlow().mapToList(Dispatchers.IO)

    suspend fun sync() {
        val remote = supabase.postgrest["transactions"]
            .select()
            .decodeList<Transaction>()

        remote.forEach { t ->
            queries.insert(
                id           = t.id,
                entity_type  = t.entityType.name,
                entity_id    = t.entityId,
                sale_id      = t.saleId,
                amount       = t.amount,
                payment_mode = t.paymentMode.name,
                reference_no = t.referenceNo,
                payment_date = t.paymentDate,
                notes        = t.notes,
                created_at   = t.createdAt,
            )
        }
    }

    suspend fun insert(txn: Transaction) {
        supabase.postgrest["transactions"].insert(txn)
        sync()
    }
}
