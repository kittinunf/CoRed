import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.`maven-publish`
import org.gradle.kotlin.dsl.signing
import java.util.Properties

plugins {
    `maven-publish`
    signing
}

// Stub secrets to let the project sync and build without the publication values set up
ext["signing.keyId"] = null
ext["signing.password"] = null
ext["signing.secretKeyRingFile"] = null
ext["sonatype.username"] = null
ext["sonatype.password"] = null
ext["sonatype.profileId"] = null

// Grabbing secrets from local.properties file or from environment variables, which could be used on CI
val secretPropsFile = project.rootProject.file("local.properties")
if (secretPropsFile.exists()) {
    secretPropsFile.reader().use {
        Properties().apply {
            load(it)
        }
    }.onEach { (name, value) ->
        ext[name.toString()] = value
    }
} else {
    ext["signing.keyId"] = System.getenv("SIGNING_KEY_ID")
    ext["signing.password"] = System.getenv("SIGNING_PASSWORD")
    ext["signing.secretKeyRingFile"] = System.getenv("SIGNING_SECRET_KEY_RING_FILE")
    ext["sonatype.username"] = System.getenv("SONATYPE_USERNAME")
    ext["sonatype.password"] = System.getenv("SONATYPE_PASSWORD")
    ext["sonatype.profileId"] = System.getenv("SONATYPE_PROFILE_ID")
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

fun getExtraString(name: String) = ext[name]?.toString()

val isReleaseBuild: Boolean
    get() = properties.containsKey("release")

publishing {
    // Configure maven central repository
    repositories {
        maven {
            name = "sonatype"
            url = uri(
                if (isReleaseBuild) {
                    "https://oss.sonatype.org/service/local/staging/deploy/maven2"
                } else {
                    "https://oss.sonatype.org/content/repositories/snapshots"
                }
            )

            credentials {
                username = getExtraString("sonatype.username")
                password = getExtraString("sonatype.password")
            }
        }
    }

    // Configure all publications
    publications.withType<MavenPublication> {
        artifactId = project.name

        artifact(javadocJar)

        // Provide artifacts information requited by Maven Central
        pom {
            name.set("CoRed")
            description.set("Opinionated Redux-like implementation backed by Kotlin Coroutines")
            url.set("https://github.com/kittinunf/CoRed")

            licenses {
                license {
                    name.set("MIT")
                    url.set("https://opensource.org/licenses/MIT")
                    distribution.set("repo")
                }
            }
            developers {
                developer {
                    id.set("kittinunf")
                    name.set("Kittinun Vantasin")
                }
            }
            scm {
                val siteUrl = "https://github.com/kittinunf/CoRed"
                val gitUrl = "https://github.com/kittinunf/CoRed.git"
                connection.set(gitUrl)
                developerConnection.set(gitUrl)
                url.set(siteUrl)
            }
        }
    }
}

signing {
    sign(publishing.publications)
}
