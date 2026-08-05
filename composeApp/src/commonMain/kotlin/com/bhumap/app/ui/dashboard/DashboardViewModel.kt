package com.bhumap.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhumap.app.data.repository.CustomerRepository
import com.bhumap.app.data.repository.LandRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

data class DashboardStats(
    val totalLands: Int     = 0,
    val totalCustomers: Int = 0,
    val totalRevenue: Double= 0.0,
    val pendingEmis: Int    = 0,
    val isLoading: Boolean  = true,
)

class DashboardViewModel(
    private val landRepo: LandRepository,
    private val customerRepo: CustomerRepository,
) : ViewModel() {

    private val _stats = MutableStateFlow(DashboardStats())
    val stats: StateFlow<DashboardStats> = _stats.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                landRepo.observeAll(),
                customerRepo.observeAll(),
            ) { lands, customers ->
                DashboardStats(
                    totalLands     = lands.size,
                    totalCustomers = customers.size,
                    totalRevenue   = lands.sumOf { it.total_cost },
                    pendingEmis    = 0, // populated from EmiRepository (Phase 2)
                    isLoading      = false,
                )
            }.collect { _stats.value = it }
        }

        // Trigger background sync
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                landRepo.sync()
                customerRepo.sync()
            }
        }
    }
}
