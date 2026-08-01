package com.bhumap.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhumap.app.data.repository.AuthRepository
import com.bhumap.app.utils.normalisePhone
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val phone: String       = "",
    val otp: String         = "",
    val isLoading: Boolean  = false,
    val error: String?      = null,
    val otpSent: Boolean    = false,
)

class AuthViewModel(private val repo: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state

    fun onPhoneChange(v: String) { _state.value = _state.value.copy(phone = v, error = null) }
    fun onOtpChange(v: String)   { _state.value = _state.value.copy(otp = v, error = null)   }

    fun sendOtp(onSent: (String) -> Unit) {
        val phone = normalisePhone(_state.value.phone)
