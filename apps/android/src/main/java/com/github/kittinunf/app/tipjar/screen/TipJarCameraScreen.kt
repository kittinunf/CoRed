package com.github.kittinunf.app.tipjar.screen

import android.Manifest
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.github.kittinunf.app.tipjar.controller.CameraCaptureController
import com.github.kittinunf.app.tipjar.util.getInstance
import com.github.kittinunf.tipjar.api.input.InputUiState
import com.github.kittinunf.tipjar.api.input.TipJarInputViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionRequired
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun TipJarCameraScreen(state: InputUiState, navigateToSettingsScreen: () -> Unit = {}, onResult: (Boolean) -> Unit) {
    require(state.isPhotoEnabled) { "This screen required the photoEnabled flag to be true" }

    var isRationaleDialogShown by rememberSaveable { mutableStateOf(true) }
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    PermissionRequired(
        permissionState = cameraPermissionState,
        permissionNotGrantedContent = {
            if (isRationaleDialogShown.not()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Camera feature not available")
                }
            } else {
                AlertComponent(title = "Camera is important to take a picture of slip, press button to grant the access",
                    confirmText = { Text("Request Permission") },
                    onConfirm = { cameraPermissionState.launchPermissionRequest() },
                    dismissText = { Text("Don't ask again") },
                    onDismiss = { isRationaleDialogShown = false }
                )
            }
        },
        permissionNotAvailableContent = {
            AlertComponent(title = "Camera permission is denied, press button to go to Settings screen and grant the access.",
                confirmText = { Text("Settings") },
                dismissText = { Text("Cancel") },
                onConfirm = navigateToSettingsScreen,
                onDismiss = {}
            )
        }
    ) {
        val vm = remember { getInstance<TipJarInputViewModel>() }
        val states by vm.states.collectAsState(rememberCoroutineScope().coroutineContext)

        // initial set
        LaunchedEffect(Unit) { vm.setInitialTip(state) }

        val context = LocalContext.current

        val controller = remember { getInstance<CameraCaptureController>() }
        CameraLayout(state = states,
            cameraCaptureController = controller,
            onResult = { photoUri ->
                if (photoUri == null) {
                    Toast.makeText(context, "Can't save photo", Toast.LENGTH_SHORT).show()
                    onResult(false)
                } else {
                    vm.saveTip(photoUri.path)
                    onResult(true)
                }
            }
        )
    }
}

@Composable
fun CameraLayout(state: InputUiState, cameraCaptureController: CameraCaptureController, onResult: (Uri?) -> Unit) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Box {
        CameraPreview(modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(),
            imageCapture = cameraCaptureController.imageCapture
        )

        Button(modifier = Modifier
            .size(120.dp, 80.dp)
            .padding(bottom = 32.dp)
            .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(50),
            onClick = {
                scope.launch {
                    try {
                        val uri = withContext(Dispatchers.IO) { cameraCaptureController.capture() }
                        onResult(uri)
                    } catch (ex: Exception) {
                        Toast.makeText(context, "Cannot capture photo : ${ex.message}, please try again later", Toast.LENGTH_SHORT).show()
                        onResult(null)
                    }
                }
            }) {
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Text("Capture")
            }
        }
    }
}

@Composable
fun AlertComponent(title: String, confirmText: @Composable () -> Unit, dismissText: @Composable () -> Unit, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = { onDismiss() },
        text = {
            Text(text = title)
        },
        confirmButton = {
            Button(onClick = {
                onConfirm()
            }) {
                confirmText()
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                dismissText()
            }
        })
}

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    imageCapture: ImageCapture
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val previewView = PreviewView(context)

            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }

                try {
                    // Must unbind the use-cases before rebinding them.
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, imageCapture, preview)
                } catch (ex: Exception) {
                    Log.e("CameraPreview", "Use case binding failed", ex)
                }
            }, ContextCompat.getMainExecutor(context))

            previewView
        })
}
