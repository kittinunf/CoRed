@file:JvmName("Koin")

package com.github.kittinunf.tipjar.api.di

import com.github.kittinunf.tipjar.api.input.InputStore
import com.github.kittinunf.tipjar.api.list.ListStore
import com.github.kittinunf.tipjar.repository.TipJarRepository
import com.github.kittinunf.tipjar.repository.TipJarRepositoryImpl
import com.github.kittinunf.tipjar.service.TipJarDBService
import kotlinx.coroutines.CoroutineScope
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import kotlin.jvm.JvmName

// Use for Android and Test
fun startKoin(appDeclaration: KoinAppDeclaration = {}, additionalModules: List<Module> = emptyList()) = startKoin {
    appDeclaration()
    modules(commonModule, platformModule(), *additionalModules.toTypedArray())
}

// Use for iOS
@Suppress("unused")
fun start() = startKoin {}

val commonModule = module {
    single { TipJarDBService(get()) }
    factory<TipJarRepository> { TipJarRepositoryImpl(get()) }

    // InputStore
    factory(named("InputStore")) { (scope: CoroutineScope) -> InputStore(get(), scope) }
    // ListStore
    factory(named("ListStore")) { (scope: CoroutineScope) -> ListStore(get(), scope) }
}

expect fun platformModule(): Module
