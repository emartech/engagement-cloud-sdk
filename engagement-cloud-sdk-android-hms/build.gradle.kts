import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SourcesJar

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.agconnect)
    alias(libs.plugins.builtInKotlin)
    alias(libs.plugins.mavenPublish)
}

dependencies {
    api(libs.hms)
    api(libs.agconnect.core)

    testImplementation(libs.kotlin.test)
    implementation(libs.mockk.android)
    implementation(libs.androidx.runner)
    implementation(libs.kotest.assertions.core)

    androidTestImplementation(libs.kotlinx.serialization.json)
    androidTestImplementation(libs.junit)
}
android {
    enableKotlin = true
    namespace = "com.sap.ec.hms"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/LICENSE-notice.md"
        }
    }

    kotlin {
        jvmToolchain(17)
    }
}

mavenPublishing {
    configure(
        AndroidSingleVariantLibrary(
            javadocJar = JavadocJar.None(),
            sourcesJar = SourcesJar.Sources(),
            variant = "release",
        )
    )

    val version = System.getenv("VERSION_OVERRIDE") ?: "4.0.0"
    coordinates("com.sap", "engagement-cloud-sdk-android-hms", version)
}

if (findProperty("ENABLE_PUBLISHING") == "true") {
    publishing {
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/${System.getenv("GITHUB_REPO") ?: "emartech/kmp-emarsys-sdk"}")
                credentials {
                    username = System.getenv("GITHUB_ACTOR")
                    password = System.getenv("GITHUB_TOKEN")
                }
            }
        }
    }
}
