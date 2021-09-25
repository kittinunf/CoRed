plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.library")
}

val artifactName = "CoRed"

kotlin {
    android()
    ios {
        binaries {
            framework {
                baseName = artifactName
            }
        }
    }
    sourceSets {
        all {
            languageSettings.apply {
            }
        }

        val commonMain by getting {
            dependencies {
                api(project(":cored"))

                implementation(Coroutines.core)
                implementation("io.ktor:ktor-client-core:1.6.3")
                implementation("io.ktor:ktor-client-serialization:1.6.3")

                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0-RC")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-okhttp:1.6.3")
            }
        }

        val androidTest by getting {
            dependencies {
                implementation(Coroutines.test)
            }
        }

        val iosMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-ios:1.6.3")
            }
        }
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

tasks {
    fun getFrameworks(buildType: String): List<String> {
        val arm64 = project.buildDir.resolve("bin/iosArm64/${buildType}Framework/$artifactName.framework").toString()
        val x64 = project.buildDir.resolve("bin/iosx64/${buildType}Framework/$artifactName.framework").toString()
        return listOf("-framework", arm64, "-debug-symbols", "$arm64.dSYM", "-framework", x64, "-debug-symbols", "$x64.dSYM")
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
