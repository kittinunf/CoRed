plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    compileSdk = 30

    defaultConfig {
        applicationId = "com.github.kittinunf.app.tipjar"
        minSdk = 24
        targetSdk = 30
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.0.0-beta08"
    }

    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-P", "plugin:androidx.compose.compiler.plugins.kotlin:suppressKotlinVersionCompatibilityCheck=true")
    }
}

dependencies {
    // link tipjar lib and dependencies
    implementation(project(":apps:shared"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
    implementation("io.insert-koin:koin-android:3.1.2")

    // core
    implementation("com.google.android.material:material:1.3.0")
    implementation("androidx.core:core-ktx:1.6.0-alpha03")

    // compose
    implementation("com.google.accompanist:accompanist-swiperefresh:0.11.1")
    implementation("androidx.activity:activity-compose:1.3.0-alpha08")
    implementation("androidx.compose.material:material:1.0.0-beta08")
    implementation("androidx.compose.ui:ui-tooling:1.1.0-alpha01")
    implementation("androidx.compose.ui:ui:1.0.0-beta08")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:1.0.0-alpha05")

    // camera
    implementation("com.google.accompanist:accompanist-permissions:0.16.0")
    implementation("androidx.camera:camera-camera2:1.1.0-alpha07")
    implementation("androidx.camera:camera-lifecycle:1.1.0-alpha07")
    implementation("androidx.camera:camera-view:1.0.0-alpha27")

    // lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.0-alpha02")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.0-alpha02")
}
