package com.bhumap.app.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.OTP
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import io.github.jan.supabase.auth.status.SessionStatus

class AuthRepository(private val supabase: SupabaseClient) {

    /** Send OTP to +91 phone number */
    suspend fun sendOtp(phone: String) {
