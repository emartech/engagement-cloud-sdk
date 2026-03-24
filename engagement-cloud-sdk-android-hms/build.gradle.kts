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
    if (project.findProperty("PROMOTE_TO_MAVEN_CENTRAL") == "true") {
        publishToMavenCentral()
    }
    if (project.hasProperty("signing.keyId") || System.getenv("GPG_PRIVATE_KEY") != null) {
        signAllPublications()
    }

    val version = System.getenv("VERSION_OVERRIDE")
    coordinates("com.sap.engagement-cloud", "engagement-cloud-sdk-android-hms", version)

    pom {
        name = "SAP Engagement Cloud SDK Android HMS"
        description = "Huawei Push Kit integration for SAP Engagement Cloud SDK"
        inceptionYear = "2025"
        url = "https://github.com/emartech/engagement-cloud-sdk/"
        licenses {
            license {
                name = "Apache License 2.0"
                url = "https://github.com/emartech/engagement-cloud-sdk/blob/main/LICENSE"
                distribution = "https://github.com/emartech/engagement-cloud-sdk/blob/main/LICENSE"
            }
        }
        developers {
            developer {
                id = "sap"
                name = "SAP"
                url = "https://sap.com"
            }
        }
        scm {
            url = "https://github.com/emartech/engagement-cloud-sdk"
            connection = "scm:git:https://github.com/emartech/engagement-cloud-sdk.git"
            developerConnection = "scm:git:https://github.com/emartech/engagement-cloud-sdk.git"
        }
    }
}

if (findProperty("ENABLE_PUBLISHING") == "true") {
    publishing {
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/${System.getenv("GITHUB_REPO") ?: "emartech/engagement-cloud-sdk"}")
                credentials {
                    username = System.getenv("GITHUB_ACTOR")
                    password = System.getenv("GITHUB_TOKEN")
                }
            }
        }
    }
}
