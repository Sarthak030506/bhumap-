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
