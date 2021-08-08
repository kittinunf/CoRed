package com.github.kittinunf.tipjar.api.list

import com.github.kittinunf.cored.Reducer
import com.github.kittinunf.cored.Store
import com.github.kittinunf.cored.StoreType
import com.github.kittinunf.tipjar.repository.TipJarRepository
import kotlinx.coroutines.CoroutineScope

data class ListUiItemState(val id: Int, val timestamp: String, val amount: Float, val tipAmount: Float, val image: String?)

data class ListUiState(val isLoading: Boolean, val list: List<ListUiItemState>, val errorMessage: String?) {
    constructor() : this(isLoading = false, list = emptyList(), errorMessage = null)
}

// region Action
internal class LoadTips(val isSortDesc: Boolean)
internal class LoadTipsResultSuccess(val value: List<ListUiItemState>)
internal class LoadTipsResultFailure(val error: Throwable)

internal class DeleteTip(id: Int)
// endregion

// region Reducer
@Suppress("FunctionName")
internal fun LoadTipsReducer() = LoadTips::class to Reducer { currentState: ListUiState, _: LoadTips ->
    with(currentState) {
        copy(isLoading = true)
    }
}

@Suppress("FunctionName")
internal fun LoadTipsResultSuccessReducer() = LoadTipsResultSuccess::class to Reducer { currentState: ListUiState, action: LoadTipsResultSuccess ->
    with(currentState) {
        copy(isLoading = false, list = action.value, errorMessage = null)
    }
}

@Suppress("FunctionName")
internal fun LoadTipsResultFailureReducer() = LoadTipsResultFailure::class to Reducer { currentState: ListUiState, action: LoadTipsResultFailure ->
    with(currentState) {
        copy(isLoading = false, errorMessage = action.error.message)
    }
}
// endregion

// region Store
internal typealias ListStore = StoreType<ListUiState>

@Suppress("FunctionName")
internal fun ListStore(repository: TipJarRepository, scope: CoroutineScope): ListStore {
    return Store(
        scope = scope,
        initialState = ListUiState(),
        reducers = mapOf(
            LoadTipsReducer(),
            LoadTipsResultSuccessReducer(),
            LoadTipsResultFailureReducer()
        ),
        middlewares = mapOf(
            LoadTipsEffect(repository, scope, ::ListUiItemStateMapper)
        )
    )
}
// endregion
