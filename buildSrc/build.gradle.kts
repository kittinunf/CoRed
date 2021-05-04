plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    jcenter()
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
}
