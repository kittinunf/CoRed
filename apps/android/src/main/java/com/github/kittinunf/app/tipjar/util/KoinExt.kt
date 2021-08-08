package com.github.kittinunf.app.tipjar.util

import org.koin.core.context.GlobalContext

inline fun <reified T : Any> getInstance() = GlobalContext.get().get<T>()
