package com.github.kittinunf.app.tipjar

import android.app.Application
import com.github.kittinunf.app.tipjar.controller.CameraCaptureController
import com.github.kittinunf.tipjar.api.di.startKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.dsl.module

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin({
            androidLogger()
            androidContext(this@App)
            modules(cameraModule)
        })
    }
}

private val cameraModule = module {
    factory {
        CameraCaptureController(get())
    }
}
