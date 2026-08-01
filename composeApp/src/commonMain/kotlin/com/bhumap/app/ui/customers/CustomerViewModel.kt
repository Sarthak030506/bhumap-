package com.bhumap.app.ui.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhumap.app.data.local.db.Customer
import com.bhumap.app.data.repository.CustomerRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CustomerUiState(
    val customers: List<Customer> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val searchQuery: String = "",
)

class CustomerViewModel(private val repo: CustomerRepository) : ViewModel() {

    private val _state = MutableStateFlow(CustomerUiState())
    val state: StateFlow<CustomerUiState> = _state.asStateFlow()
