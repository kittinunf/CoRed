package com.github.kittinunf.tipjar.api.input

import com.github.kittinunf.tipjar.api.NativeViewModel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named

class TipJarInputViewModel : NativeViewModel(), KoinComponent {

    private val store by inject<InputStore>(named("InputStore")) { parametersOf(scope) }

    @Suppress("Unused")
    val currentState
        get() = store.currentState

    val states = store.states

    fun isCurrentStateValid(): Boolean = currentState.totalTipAmount != 0f && currentState.amount != 0f

    fun setInitialTip(state: InputUiState? = null) {
        scope.launch {
            val action = if (state == null) SetTip() else SetTip(state)
            store.dispatch(action)
        }
    }

    fun updateAmount(amount: Float) {
        scope.launch {
            store.dispatch(UpdateAmount(amount))
        }
    }

    fun updatePeopleCount(count: Int) {
        scope.launch {
            store.dispatch(UpdatePeopleCount(count))
        }
    }

    fun updateTipPercentage(tipPercentage: Int) {
        scope.launch {
            store.dispatch(UpdateTipPercentage(tipPercentage))
        }
    }

    fun updatePhotoEnabled(isPhotoEnabled: Boolean) {
        scope.launch {
            store.dispatch(UpdatePhotoEnabled(isPhotoEnabled))
        }
    }

    fun saveTip(photoLocation: String? = null) {
        scope.launch {
            store.dispatch(SaveTip(photoLocation))
        }
    }
}
