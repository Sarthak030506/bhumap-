package com.bhumap.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhumap.app.data.repository.CustomerRepository
import com.bhumap.app.data.repository.LandRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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
