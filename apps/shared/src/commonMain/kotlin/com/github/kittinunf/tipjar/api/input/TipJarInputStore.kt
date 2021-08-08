package com.github.kittinunf.tipjar.api.input

import com.github.kittinunf.cored.NoopReducer
import com.github.kittinunf.cored.Reducer
import com.github.kittinunf.cored.Store
import com.github.kittinunf.cored.StoreType
import com.github.kittinunf.tipjar.repository.TipJarRepository
import kotlinx.coroutines.CoroutineScope

data class InputUiState(val amount: Float, val peopleCount: Int, val tipPercentage: Int, val totalTipAmount: Float, val tipPerPerson: Float, val isPhotoEnabled: Boolean) {
    constructor() : this(0f, 0, 0, 0f, 0f, false)
}

// region Action
internal class SetTip(val state: InputUiState = InputUiState(amount = 0.0f, peopleCount = 1, tipPercentage = 0, totalTipAmount = 0f, tipPerPerson = 0f, isPhotoEnabled = false))
internal class UpdateAmount(val amount: Float) {
    init {
        require(amount >= 0) { "$amount is not greater or equal than 0" }
    }
}

internal class UpdatePeopleCount(val count: Int) {
    init {
        require(count > 0) { "$count is not bigger than 0" }
    }
}

internal class UpdateTipPercentage(val percentage: Int) {
    init {
        require(percentage in (0..100)) { "$percentage is not in the range of 0..100" }
    }
}

internal class UpdatePhotoEnabled(val isEnabled: Boolean)
internal class SaveTip(val photoLocation: String? = null)

internal object SaveTipResultSuccess
internal class SaveTipResultError(val message: String)
// endregion

// region Reducer
@Suppress("FunctionName")
internal fun SetTipReducer() = SetTip::class to Reducer { currentState: InputUiState, action: SetTip ->
    with(currentState) {
        val state = action.state
        copy(amount = state.amount, peopleCount = state.peopleCount, tipPercentage = state.tipPercentage, totalTipAmount = state.totalTipAmount, tipPerPerson = state.tipPerPerson, isPhotoEnabled = state.isPhotoEnabled)
    }
}

@Suppress("FunctionName")
internal fun UpdateAmountReducer() = UpdateAmount::class to Reducer { currentState: InputUiState, action: UpdateAmount ->
    with(currentState) {
        val newTotalTipAmount = (action.amount * tipPercentage.toFloat()) / 100f
        val newTipPerPerson = newTotalTipAmount / peopleCount.toFloat()
        copy(amount = action.amount, totalTipAmount = newTotalTipAmount, tipPerPerson = newTipPerPerson)
    }
}

@Suppress("FunctionName")
internal fun UpdatePeopleCountReducer() = UpdatePeopleCount::class to Reducer { currentState: InputUiState, action: UpdatePeopleCount ->
    with(currentState) {
        val newTipPerPerson = totalTipAmount / action.count
        copy(peopleCount = action.count, tipPerPerson = newTipPerPerson)
    }
}

@Suppress("FunctionName")
internal fun UpdateTipPercentageReducer() = UpdateTipPercentage::class to Reducer { currentState: InputUiState, action: UpdateTipPercentage ->
    with(currentState) {
        val newTotalTipAmount = (amount * action.percentage.toFloat()) / 100f
        val newTipPerPerson = newTotalTipAmount / peopleCount.toFloat()
        copy(tipPercentage = action.percentage, totalTipAmount = newTotalTipAmount, tipPerPerson = newTipPerPerson)
    }
}

@Suppress("FunctionName")
internal fun UpdatePhotoEnableReducer() = UpdatePhotoEnabled::class to Reducer { currentState: InputUiState, action: UpdatePhotoEnabled ->
    with(currentState) {
        copy(isPhotoEnabled = action.isEnabled)
    }
}

@Suppress("FunctionName")
internal fun SaveTipReducer() = SaveTip::class to NoopReducer<InputUiState>()

@Suppress("FunctionName")
internal fun SaveTipResultSuccessReducer() = SaveTipResultSuccess::class to NoopReducer<InputUiState>()
// endregion

// region Store
internal typealias InputStore = StoreType<InputUiState>

@Suppress("FunctionName")
internal fun InputStore(repository: TipJarRepository, scope: CoroutineScope): InputStore {
    return Store(
        scope = scope,
        initialState = InputUiState(),
        reducers = mapOf(
            SetTipReducer(),
            UpdateAmountReducer(),
            UpdatePeopleCountReducer(),
            UpdateTipPercentageReducer(),
            UpdatePhotoEnableReducer(),
            SaveTipReducer(),
            SaveTipResultSuccessReducer()
        ),
        middlewares = mapOf(
            SaveTipEffect(repository, scope)
        )
    )
}
// endregion
