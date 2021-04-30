object Kotlin {

    private const val version = "1.4.31"

    const val plugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$version"

    const val testCommon = "org.jetbrains.kotlin:kotlin-test-common"
    const val testAnnotationsCommon = "org.jetbrains.kotlin:kotlin-test-annotations-common"
    const val testJunit = "org.jetbrains.kotlin:kotlin-test-junit"
}

object Android {

    const val minSdkVersion = 24
    const val targetSdkVersion = 30
    const val compileSdkVersion = 30

    const val buildToolsVersion = "30.0.3"

    private const val version = "7.0.0-alpha14"
    const val plugin = "com.android.tools.build:gradle:$version"
}

object AndroidX {

    object Versions {
        const val junit = "1.1.1"
    }

    const val testJunit = "androidx.test.ext:junit:${Versions.junit}"
}

object Coroutines {

    private const val mtVersion = "1.4.3-native-mt"
    private const val version = "1.4.3"

    const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$mtVersion"
    const val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$version"

    const val test = "org.jetbrains.kotlinx:kotlinx-coroutines-test:$version"
}

// Test libraries
object JUnit {

    private const val version = "4.13.1"
    private const val jacocoVersion = "0.16.0"

    const val jvm = "junit:junit:$version"
    const val pluginJacoco = "gradle.plugin.com.vanniktech:gradle-android-junit-jacoco-plugin:$jacocoVersion"
}

object Jacoco {
    const val version = "0.8.6"
}
