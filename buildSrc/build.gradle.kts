plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    google()
    maven { setUrl("https://plugins.gradle.org/m2/") }
}

kotlin {
    val main by sourceSets.getting {
        kotlin.srcDir("buildSrc/src/main/kotlin")
    }
}

dependencies {
    // main
    implementation(Kotlin.plugin)

    // plugins
    implementation(Android.plugin)
    implementation(JUnit.pluginJacoco)

    // app
    implementation("com.squareup.sqldelight:gradle-plugin:1.5.0")
}
