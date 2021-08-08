package com.github.kittinunf.tipjar.api.di

import com.github.kittinunf.tipjar.api.input.TipJarInputViewModel
import com.github.kittinunf.tipjar.api.list.TipJarListViewModel
import com.github.kittinunf.tipjar.db.TipJarDB
import com.github.kittinunf.tipjar.service.createSqlDriver
import com.github.kittinunf.tipjar.service.filename
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single {
        TipJarDB(createSqlDriver(Unit, filename))
    }

    factory { TipJarInputViewModel() }
    factory { TipJarListViewModel() }
}
