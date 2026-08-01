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
