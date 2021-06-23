package com.github.kittinunf.cored.app.api

import com.github.kittinunf.cored.StoreType
import com.github.kittinunf.cored.app.repository.UserRepositoryImpl
import kotlinx.coroutines.CoroutineScope

class AppViewModel(scope: CoroutineScope) {

    private val store: StoreType<AppState> by lazy { AppStore(scope, UserRepositoryImpl()) }

    @Suppress("Unused")
    val currentState
        get() = store.currentState

    val states = store.states
}
