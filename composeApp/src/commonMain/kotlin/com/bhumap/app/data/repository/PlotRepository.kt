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
