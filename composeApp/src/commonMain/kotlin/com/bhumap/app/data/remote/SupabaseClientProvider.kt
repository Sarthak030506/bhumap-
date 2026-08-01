package com.bhumap.app.data.remote

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

/**
 * Supabase client singleton.
 * URL and anon key are injected by Koin from BuildConfig / environment.
 */
fun createSupabase(url: String, anonKey: String) = createSupabaseClient(
