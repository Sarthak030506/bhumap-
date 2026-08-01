package com.bhumap.app.data.repository

import com.bhumap.app.data.local.db.BhumapDatabase
import com.bhumap.app.domain.model.Customer
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

class CustomerRepository(
    private val db: BhumapDatabase,
    private val supabase: SupabaseClient,
) {
    private val queries get() = db.customerQueries

    fun observeAll() = queries.selectAll().asFlow().mapToList(Dispatchers.IO)

    suspend fun sync() {
        val remote = supabase.postgrest["customers"]
            .select()
            .decodeList<Customer>()

        remote.forEach { c ->
            queries.upsert(
                id         = c.id,
                name       = c.name,
                phone      = c.phone,
                email      = c.email,
                aadhaar    = c.aadhaar,
                address    = c.address,
                created_at = c.createdAt,
                updated_at = c.updatedAt,
            )
        }
    }

    suspend fun insert(customer: Customer) {
        supabase.postgrest["customers"].insert(customer)
        sync()
    }
}
