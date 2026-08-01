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
