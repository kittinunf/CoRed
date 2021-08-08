package com.github.kittinunf.tipjar.api.input

import com.github.kittinunf.cored.EffectType
import com.github.kittinunf.cored.Middleware
import com.github.kittinunf.cored.Order
import com.github.kittinunf.tipjar.repository.TipJarRepository
import com.github.kittinunf.tipjar.util.getCurrentTimestampInSeconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Suppress("FunctionName")
internal fun SaveTipEffect(repository: TipJarRepository, scope: CoroutineScope): EffectType<InputUiState, SaveTip> = SaveTip::class to Middleware { order, store, state, action ->
    if (order == Order.BeforeReduce) return@Middleware

    scope.launch {
        try {
            // save tip into the database
            // amount is saved as Int number in the DB so we covert by times 100 to avoid 2 decimal places
            repository.createTip((state.amount * 100f).toInt(), state.peopleCount, (state.totalTipAmount * 100f).toInt(), action.photoLocation, getCurrentTimestampInSeconds())
            store.dispatch(SaveTipResultSuccess)
        } catch (e: Exception) {
            store.dispatch(SaveTipResultError(e.message ?: "Save tip error with unknown message"))
        }
    }
}
