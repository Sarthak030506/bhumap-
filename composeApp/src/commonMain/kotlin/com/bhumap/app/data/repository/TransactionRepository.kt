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
