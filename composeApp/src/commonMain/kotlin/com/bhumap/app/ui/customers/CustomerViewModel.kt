package com.bhumap.app.ui.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhumap.app.data.local.db.Customer
import com.bhumap.app.data.repository.CustomerRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

data class CustomerUiState(
    val customers: List<Customer> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val searchQuery: String = "",
)

class CustomerViewModel(private val repo: CustomerRepository) : ViewModel() {

    private val _state = MutableStateFlow(CustomerUiState())
    val state: StateFlow<CustomerUiState> = _state.asStateFlow()

    val filtered: StateFlow<List<Customer>> = combine(_state, _state) { s, _ ->
        if (s.searchQuery.isBlank()) s.customers
        else s.customers.filter { c ->
            c.name.contains(s.searchQuery, ignoreCase = true) ||
            c.phone.contains(s.searchQuery)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        viewModelScope.launch {
            repo.observeAll().collect { list ->
                _state.update { it.copy(customers = list, isLoading = false) }
            }
        }
        viewModelScope.launch(Dispatchers.IO) { runCatching { repo.sync() } }
    }

    fun onSearchChange(q: String) = _state.update { it.copy(searchQuery = q) }
}
