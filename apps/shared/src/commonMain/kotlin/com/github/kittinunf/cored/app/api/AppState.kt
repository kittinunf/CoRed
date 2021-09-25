package com.github.kittinunf.cored.app.api

import kotlinx.serialization.Serializable

@Serializable
data class User(val id: Int, val name: String, val email: String)

data class AppState(val isLoading: Boolean = false, val users: List<User>? = null)
