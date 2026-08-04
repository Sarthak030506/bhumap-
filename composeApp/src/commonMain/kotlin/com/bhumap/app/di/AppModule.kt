package com.bhumap.app.di

import com.bhumap.app.data.repository.AuthRepository
import com.bhumap.app.data.repository.CustomerRepository
import com.bhumap.app.data.repository.EmiRepository
import com.bhumap.app.data.repository.FarmerRepository
import com.bhumap.app.data.repository.LandRepository
import com.bhumap.app.data.repository.PartnerRepository
import com.bhumap.app.data.repository.PlotRepository
import com.bhumap.app.data.repository.SaleRepository
import com.bhumap.app.data.repository.TransactionRepository
import com.bhumap.app.ui.auth.AuthViewModel
import com.bhumap.app.ui.customers.CustomerViewModel
import com.bhumap.app.ui.dashboard.DashboardViewModel
import com.bhumap.app.ui.land.LandViewModel
import com.bhumap.app.ui.map.MapViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    // ─── Repositories ─────────────────────────────────────────────────────────
    single { AuthRepository(get()) }
    single { LandRepository(get(), get()) }
    single { CustomerRepository(get(), get()) }
    single { PlotRepository(get(), get()) }
    single { TransactionRepository(get(), get()) }
    single { SaleRepository(get(), get()) }
    single { EmiRepository(get(), get()) }
    single { PartnerRepository(get(), get()) }
    single { FarmerRepository(get(), get()) }

    // ─── ViewModels ───────────────────────────────────────────────────────────
    viewModelOf(::AuthViewModel)
    viewModelOf(::DashboardViewModel)
    viewModelOf(::LandViewModel)
    viewModelOf(::MapViewModel)
    viewModelOf(::CustomerViewModel)
}
