// Main libraries
object Kotlin {

    private const val version = "1.5.0"

    const val plugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$version"
}

object Android {

    const val minSdkVersion = 24
    const val targetSdkVersion = 30
    const val compileSdkVersion = 30

    private const val version = "7.0.0-beta01"
    const val plugin = "com.android.tools.build:gradle:$version"
}

object AndroidX {

    object Versions {
        const val junit = "1.1.1"
    }
}

object Coroutines {

    private const val version = "1.5.0-RC-native-mt"

    const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$version"
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
    const val version = "0.8.7"
}

// Publishing libraries
object Publishing {
   
    const val groupId = "com.github.kittinunf.cored"
    const val version = "0.2.0"
}

object GradleNexus {

    private const val version = "1.1.0"

    const val pluginNexus = "io.github.gradle-nexus:publish-plugin:$version"
}
