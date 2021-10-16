package com.github.kittinunf.cored.app.api

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class AppStoreTest {

    @Test
    fun `should reduce state to load with LoadUserReducer`() {
        val state = AppState()

        val (_, reducer) = LoadUserReducer()

        val newState = reducer(state, Load)
        assertEquals(false, state.isLoading)
        assertEquals(true, newState.isLoading)
    }

    @Test
    fun `should reduce state to set users with SetUsersReducer`() {
        val state = AppState()

        val (_, reducer) = SetUsersReducer()

        val users = listOf(User(1, "foo", "foo@gmail.com"), User(2, "bar", "bar@gmail.com"), User(3, "foobar", "foobar@gmail.com"))
        val newState = reducer(state, SetUsers(users))
        assertContentEquals(newState.users, users)
    }
}
