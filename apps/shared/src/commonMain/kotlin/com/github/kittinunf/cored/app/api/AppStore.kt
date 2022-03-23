package com.github.kittinunf.cored.app.api

import com.github.kittinunf.cored.ActionMiddleware
import com.github.kittinunf.cored.ActionReducer
import com.github.kittinunf.cored.Middleware
import com.github.kittinunf.cored.Reducer
import com.github.kittinunf.cored.app.repository.UserRepository
import com.github.kittinunf.cored.store.Store
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// region Action
internal object Load
class SetUsers(val users: List<User>?)
// endregion

// region Reducer
@Suppress("FunctionName")
internal fun LoadUserReducer(): ActionReducer<AppState, Load> = Load::class to Reducer { currentState, _ ->
    currentState.copy(isLoading = true)
}

@Suppress("FunctionName")
internal fun SetUsersReducer(): ActionReducer<AppState, SetUsers> = SetUsers::class to Reducer { currentState, action ->
    currentState.copy(isLoading = false, users = action.users)
}
// endregion

// region Middleware
@Suppress("FunctionName")
internal fun LoadUserEffect(scope: CoroutineScope, repository: UserRepository): ActionMiddleware<AppState, Load> =
    Load::class to Middleware { _, store, state, _ ->
        if (state.isLoading) return@Middleware

        scope.launch {
            val users = repository.getUsers()
            store.dispatch(SetUsers(users))
        }
    }

// endregion
@Suppress("FunctionName")
internal fun AppStore(scope: CoroutineScope, repository: UserRepository) =
    Store(
        initialState = AppState(),
        reducers = mapOf(LoadUserReducer(), SetUsersReducer()),
        middlewares = mapOf(LoadUserEffect(scope, repository))
    )
