package com.bhumap.app.di

import com.bhumap.app.data.repository.AuthRepository
import com.bhumap.app.data.repository.CustomerRepository
import com.bhumap.app.data.repository.LandRepository
import com.bhumap.app.data.repository.PlotRepository
import com.bhumap.app.ui.auth.AuthViewModel
import com.bhumap.app.ui.customers.CustomerViewModel
import com.bhumap.app.ui.dashboard.DashboardViewModel
import com.bhumap.app.ui.land.LandViewModel
import com.bhumap.app.ui.map.MapViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

