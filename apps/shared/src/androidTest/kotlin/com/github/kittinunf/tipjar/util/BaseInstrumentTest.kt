package com.github.kittinunf.tipjar.util

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.test.BeforeTest

@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.P])
@RunWith(AndroidJUnit4::class)
actual abstract class BaseInstrumentTest {

    @BeforeTest
    actual abstract fun setUp()
}

actual fun initialize(): Any = ApplicationProvider.getApplicationContext()
