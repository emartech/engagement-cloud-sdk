import co.touchlab.skie.configuration.DefaultArgumentInterop
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.mokkery)
    alias(libs.plugins.skie)
    alias(libs.plugins.kmmbridge)
}

kotlin {
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "EmarsysNotificationService"
            isStatic = true
        }
    }

    sourceSets {
        iosMain {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
            }
        }
        iosTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotest.assertions.core)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}

kmmbridge {
    val spmBuildType = System.getenv("SPM_BUILD") ?: "dev"
    when (spmBuildType) {
        "dev" -> {
            println("Building for local SPM development")
            spm()
        }

        "release" -> {
            println("Building for release")
            spm(
                spmDirectory = "./iosReleaseSpm",
                useCustomPackageFile = true,
                perModuleVariablesBlock = true
            )
        }

        else -> {
            println("Unknown SPM build type: $spmBuildType. Defaulting to local SPM development.")
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
        produceDistributableFramework()
    }
    analytics {
        disableUpload.set(true)
    }
}
val deviceName = project.findProperty("iosDevice") as? String ?: "iPhone 18 Pro"


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