package com.github.kittinunf.tipjar.api.di

import com.github.kittinunf.tipjar.api.input.InputStore
import com.github.kittinunf.tipjar.api.list.ListStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import platform.CoreGraphics.CGPDFOperatorTableRetain

actual fun platformTestModule(): Module = module {
//    factory(named("InputStore")) { InputStore(get(), CoroutineScope(Dispatchers.Unconfined)) }
//    factory(named("ListStore")) { ListStore(get(), CoroutineScope(Dispatchers.Unconfined)) }
}
