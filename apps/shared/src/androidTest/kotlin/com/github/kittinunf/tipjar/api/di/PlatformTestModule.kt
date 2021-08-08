package com.github.kittinunf.tipjar.api.di

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformTestModule(): Module = module {
    single {
        ApplicationProvider.getApplicationContext<Application>()
    }
}
