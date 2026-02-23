import co.touchlab.kmmbridge.KmmBridgeExtension
import co.touchlab.skie.configuration.DefaultArgumentInterop
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.mokkery)
    alias(libs.plugins.skie)
    alias(libs.plugins.kmmbridge) apply false
    `maven-publish`
}

group = "com.sap"
version = System.getenv("VERSION_OVERRIDE") ?: "4.0.0"

val isMac = System.getProperty("os.name").contains("Mac", ignoreCase = true)
if (isMac) {
    apply(plugin = "co.touchlab.kmmbridge")
}

kotlin {
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        if (isMac) {
            iosTarget.binaries.framework {
                baseName = "EngagementCloudNotificationService"
                isStatic = true
            }
        }
    }

    if (!isMac) {
        jvm()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotest.assertions.core)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        val artifactId = artifactId.replace("ios-notification-service", "engagement-cloud-sdk-ios-notification-service")
        this.artifactId = artifactId
    }
}

if (isMac) {
    extensions.configure<KmmBridgeExtension>("kmmbridge") {
        frameworkName.set("EngagementCloudNotificationService")
        val spmBuildType = System.getenv("SPM_BUILD") ?: "dev"
        when (spmBuildType) {
            "dev" -> {
                println("Building for local SPM development")
                spm()
            }

            "release" -> {
                println("Building for release")
                mavenPublishArtifacts()
                spm()
            }

            else -> {
                println("Unknown SPM build type: $spmBuildType. Defaulting to local SPM development.")
            }
        }
    }
}

skie {
    features {
        group {
            DefaultArgumentInterop.Enabled(true)
        }
    }

    build {
        if (project.findProperty("ENABLE_PUBLISHING") == "true") {
            produceDistributableFramework()
        }
    }
    analytics {
        disableUpload.set(true)
    }
}
if (project.findProperty("ENABLE_PUBLISHING") == "true") {
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

val deviceName = project.findProperty("iosDevice") as? String ?: "iPhone 17 Pro"


tasks.register<Exec>("bootIOSSimulator") {
    isIgnoreExitValue = true
    standardOutput = System.out
    commandLine("xcrun", "simctl", "boot", deviceName)
    val invalidDeviceError = 148
    val deviceAlreadyBootedError = 149
    doLast {
        val result = executionResult.get()
        if (result.exitValue != invalidDeviceError && result.exitValue != deviceAlreadyBootedError) {
            result.assertNormalExitValue()
        }
    }
}

tasks.withType<KotlinNativeSimulatorTest>().configureEach {
    dependsOn("bootIOSSimulator")
    standalone.set(false)
    device.set(deviceName)
}