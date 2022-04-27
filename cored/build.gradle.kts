import java.io.IOException

plugins {
    kotlin("multiplatform")
    id("com.android.library")

    id("publication")
}

val isReleaseBuild: Boolean
    get() = properties.containsKey("release")

group = Publishing.groupId
val gitSha = "git rev-parse --short HEAD".runCommand(project.rootDir)?.trim().orEmpty()
version = if (isReleaseBuild) Publishing.version else "main-$gitSha-SNAPSHOT"

kotlin {
    android {
        publishLibraryVariants("release", "debug")
    }

    ios()
    iosSimulatorArm64()

    jvm()

    sourceSets {
        all {
            languageSettings.apply {
                optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
                optIn("kotlinx.coroutines.OptInAnnotation")
            }
        }

        val commonMain by getting {
            dependencies {
                implementation(Coroutines.core)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(Coroutines.test)
            }
        }

        val androidMain by getting {}

        val androidTest by getting {}

        val iosMain by getting {}

        val iosTest by getting {}
    }
}

android {
    compileSdk = Android.compileSdkVersion

    sourceSets {
        getByName("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
            java.srcDirs("src/androidMain/kotlin")
            res.srcDirs("src/androidMain/res")
        }

        getByName("androidTest") {
            manifest.srcFile("src/androidTest/AndroidManifest.xml")
            java.srcDirs("src/androidTest/kotlin")
            res.srcDirs("src/androidTest/res")
        }
    }

    defaultConfig {
        minSdk = Android.minSdkVersion
        targetSdk = Android.targetSdkVersion
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}

fun String.runCommand(workingDir: File): String? = try {
    val parts = split("\\s".toRegex())
    val proc = ProcessBuilder(*parts.toTypedArray())
        .directory(workingDir)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()

    proc.waitFor(30, TimeUnit.SECONDS)
    proc.inputStream.bufferedReader().readText()
} catch (e: IOException) {
    e.printStackTrace()
    null
}
