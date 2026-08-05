package com.bhumap.app.di

import com.bhumap.app.BuildConfig
import com.bhumap.app.data.remote.createSupabase
import org.koin.dsl.module

val networkModule = module {
    single(createdAtStart = true) { createSupabase(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_ANON_KEY) }
}
