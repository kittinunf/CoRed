pluginManagement {
    repositories {
        mavenCentral()
        google()

        maven { setUrl("https://dl.bintray.com/kotlin/kotlin") }
        maven { setUrl("https://kotlin.bintray.com/kotlinx") }
    }
}

includeBuild("plugins")

include(":cored")
