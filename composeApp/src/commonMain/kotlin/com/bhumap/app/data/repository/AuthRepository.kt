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
        supabase.auth.signInWith(OTP) {
            this.phone = phone
        }
    }

    /** Verify OTP token received via SMS */
    suspend fun verifyOtp(phone: String, token: String) {
        supabase.auth.verifyPhoneOtp(
            type  = io.github.jan.supabase.auth.OtpType.Phone.SMS,
            phone = phone,
            token = token,
        )
    }
