package com.github.kittinunf.app.tipjar

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.github.kittinunf.app.tipjar.screen.TipJarCameraScreen
import com.github.kittinunf.app.tipjar.screen.TipJarInputScreen
import com.github.kittinunf.app.tipjar.screen.TipJarListScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen(navigateToSettingsScreen = this::startSettingActivity)
        }
    }

    private fun startSettingActivity() {
        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null)))
    }
}

@Composable
fun MainScreen(navigateToSettingsScreen: () -> Unit) {
    var navigationStateScreen by remember { mutableStateOf<NavigationStateScreen>(NavigationStateScreen.Input) }

    LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher?.apply {
        addCallback(owner = LocalLifecycleOwner.current, enabled = true) {
            isEnabled = navigationStateScreen.isBackEnabled
            // perform then we go back to input
            navigationStateScreen = NavigationStateScreen.Input
        }
    }

    Scaffold(topBar = {
        TopAppBar(title = {
            Text(text = navigationStateScreen.title)
        }, actions = {
            when (navigationStateScreen) {
                NavigationStateScreen.Input -> {
                    IconButton(onClick = {
                        navigationStateScreen = NavigationStateScreen.List
                    }, content = {
                        Icon(imageVector = Icons.Default.List, contentDescription = null)
                    })
                }
                else -> {
                }
            }
        })
    }) {
        Crossfade(targetState = navigationStateScreen) { currentState ->
            when (currentState) {
                NavigationStateScreen.Input -> TipJarInputScreen(onSave = { state ->
                    navigationStateScreen = if (state.isPhotoEnabled) NavigationStateScreen.Camera(state) else NavigationStateScreen.List
                })

                is NavigationStateScreen.Camera -> TipJarCameraScreen(state = currentState.state,
                    navigateToSettingsScreen = navigateToSettingsScreen,
                    onResult = { isSuccess ->
                        navigationStateScreen = if (isSuccess) {
                            NavigationStateScreen.List
                        } else {
                            NavigationStateScreen.Input
                        }
                    })

                NavigationStateScreen.List -> TipJarListScreen()
            }
        }
    }
}
