package com.github.kittinunf.tipjar.api.list

import com.github.kittinunf.cored.EffectType
import com.github.kittinunf.cored.Middleware
import com.github.kittinunf.cored.Order
import com.github.kittinunf.tipjar.repository.TipJarRepository
import com.squareup.sqldelight.Query
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Suppress("FunctionName")
internal fun LoadTipsEffect(repository: TipJarRepository, scope: CoroutineScope, mapper: Mapper): EffectType<ListUiState, LoadTips> = LoadTips::class to Middleware { order, store, _, action ->
    if (order == Order.BeforeReduce) return@Middleware

    scope.launch {
        try {
            // load tips from the database
            val query = if (action.isSortDesc) repository.getTipsDesc(mapper) else repository.getTips(mapper)
            val listener = object : Query.Listener {
                override fun queryResultsChanged() {
                    val tips = query.executeAsList()
                    store.tryDispatch(LoadTipsResultSuccess(tips))
                }
            }
            query.addListener(listener)

            if (isActive) {
                val tips = query.executeAsList()
                store.dispatch(LoadTipsResultSuccess(tips))
            } else {
                query.removeListener(listener)
                ensureActive()
            }
        } catch (ignored: CancellationException) {
        } catch (e: Exception) {
            store.dispatch(LoadTipsResultFailure(e))
        }
    }
}
