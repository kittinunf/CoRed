package com.github.kittinunf.cored.app.api

import com.github.kittinunf.cored.store.Store
import com.github.kittinunf.cored.app.repository.UserRepositoryImpl
import kotlinx.coroutines.CoroutineScope

class AppViewModel(private val scope: CoroutineScope) {

    private val store: Store<AppState> by lazy { AppStore(scope, UserRepositoryImpl()) }

    @Suppress("Unused")
    val currentState
        get() = store.currentState

    val states = store.states
}
