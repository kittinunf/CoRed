plugins {
    kotlin("multiplatform")
    id("com.squareup.sqldelight")
    id("com.android.library")
}

val artifactName = "TipJar"

kotlin {
    android()
    ios {
        binaries {
            framework {
                baseName = artifactName

                // for Sqlite
                linkerOpts.add("-lsqlite3")
            }
        }
    }
    sourceSets {
        all {
            languageSettings.apply {
                useExperimentalAnnotation("kotlin.RequiresOptIn")
            }
        }

        val commonMain by getting {
            dependencies {
                api(project(":cored"))

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0-native-mt")
                implementation("io.insert-koin:koin-core:3.1.2")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("com.squareup.sqldelight:android-driver:1.5.0")
                implementation("io.insert-koin:koin-android:3.1.2")
                implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.0-alpha02")
            }
        }

        val androidTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.5.0-native-mt")

                implementation("androidx.test:core:1.4.0")
                implementation("androidx.test:runner:1.4.0")
                implementation("androidx.test:rules:1.4.0")
                implementation("androidx.test.ext:junit:1.1.3")
                implementation("org.robolectric:robolectric:4.5.1")
            }
        }

        val iosMain by getting {
            dependencies {
                implementation("com.squareup.sqldelight:native-driver:1.5.0")
            }
        }
    }
}

android {
    compileSdk = 30

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
        minSdk = 24
        targetSdk = 30
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}

group = "com.github.kittinunf.tipjar"
sqldelight {
    database("TipJarDB") {
        packageName = "$group.db"
        sourceFolders = listOf("resources")
    }
}

tasks {
    fun getFrameworks(buildType: String): List<String> {
        val arm64 = project.buildDir.resolve("bin/iosArm64/${buildType}Framework/$artifactName.framework").toString()
        val x64 = project.buildDir.resolve("bin/iosx64/${buildType}Framework/$artifactName.framework").toString()
        return listOf(
            "-framework", arm64,
            "-debug-symbols", "$arm64.dSYM",
            "-framework", x64,
            "-debug-symbols", "$x64.dSYM"
        )
    }

    fun getTaskCommand(buildType: String, outputDir: File): List<String> {
        val name = "$artifactName-$buildType.xcframework"
        return listOf("xcodebuild", "-create-xcframework") + getFrameworks(buildType) + listOf("-output", File("$outputDir/$name").toString())
    }

    val createXCFramework by registering {
        val buildTypes = listOf("debug", "release")
        val output = project.buildDir.resolve("bin/ios")

        if (output.exists()) project.delete(output)

        doLast {
            buildTypes.forEach { buildType ->
                project.exec {
                    commandLine = getTaskCommand(buildType, output)
                }
            }
        }
    }
}
