package com.github.kittinunf.app.tipjar

import com.github.kittinunf.tipjar.api.input.InputUiState

sealed class NavigationStateScreen(val title: String, val isBackEnabled: Boolean) {
    object Input : NavigationStateScreen("TipJar", false)
    class Camera(val state: InputUiState) : NavigationStateScreen("Record your payment", true)
    object List : NavigationStateScreen("Saved Payments", true)
}
