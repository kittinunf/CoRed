package com.github.kittinunf.tipjar.api

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

actual open class NativeViewModel : ViewModel() {

    actual val scope = viewModelScope

    actual fun cancel() {
    }
}
