package com.github.kittinunf.tipjar.api.list

import com.github.kittinunf.tipjar.api.NativeViewModel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named

class TipJarListViewModel : NativeViewModel(), KoinComponent {

    private val store by inject<ListStore>(named("ListStore")) { parametersOf(scope) }

    @Suppress("Unused")
    val currentState
        get() = store.currentState

    val states = store.states

    fun loadTips(isSortDesc: Boolean = true) {
        scope.launch {
            store.dispatch(LoadTips(isSortDesc))
        }
    }
}
