package com.bhumap.app.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.OTP
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
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

    /** Sign out and clear local session */
    suspend fun signOut() {
        supabase.auth.signOut()
    }

    /**
     * Raw session status — includes LoadingFromStorage (transient on cold start),
     * Authenticated, NotAuthenticated, and NetworkError states.
     * Prefer this over isLoggedIn for UI gating to avoid the auth flash on cold start.
     */
    val sessionStatusFlow: StateFlow<SessionStatus> = supabase.auth.sessionStatus

    /** Current session as a Flow — emits null when signed out */
    val sessionFlow: Flow<Boolean> = supabase.auth.sessionStatus.map { status ->
        status is SessionStatus.Authenticated
    }

    val isLoggedIn: Boolean
        get() = supabase.auth.currentSessionOrNull() != null
}
