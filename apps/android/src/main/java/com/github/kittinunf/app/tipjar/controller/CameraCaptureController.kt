package com.github.kittinunf.app.tipjar.controller

import android.content.Context
import android.net.Uri
import android.view.Surface.ROTATION_0
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import java.io.File
import java.util.concurrent.Executors
import kotlin.coroutines.suspendCoroutine

class CameraCaptureController(private val context: Context) {

    companion object {
        fun createFile(folder: File) = File(folder, "${System.currentTimeMillis()}.png")
    }

    val imageCapture by lazy {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetRotation(ROTATION_0)
            .build()
    }

    private fun getOutputDirectory(): File {
        return context.filesDir.let {
            File(it, "images").apply {
                if (exists().not()) mkdirs()
            }
        }
    }

    suspend fun capture(): Uri {
        val photoFile = createFile(getOutputDirectory())
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        return suspendCoroutine { cont ->
            imageCapture.takePicture(outputOptions, Executors.newSingleThreadExecutor(),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        val captureFileUri = output.savedUri ?: Uri.fromFile(photoFile)
                        if (captureFileUri == null) {
                            cont.resumeWith(Result.failure(RuntimeException("Cannot capture photo")))
                        } else {
                            cont.resumeWith(Result.success(captureFileUri))
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        cont.resumeWith(Result.failure(exception))
                    }
                })
        }
    }
}
