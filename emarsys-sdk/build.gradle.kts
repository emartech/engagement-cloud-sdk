import co.touchlab.skie.configuration.DefaultArgumentInterop
import com.android.build.gradle.internal.lint.AndroidLintAnalysisTask
import com.github.gmazzo.buildconfig.BuildConfigTask

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.dokka)
    alias(libs.plugins.ksp)
    alias(libs.plugins.buildConfig)
    alias(libs.plugins.mokkery)
    alias(libs.plugins.skie)
    alias(libs.plugins.sqlDelight)
}

group = "com.emarsys"
version = "1.0-SNAPSHOT"

kotlin {
    jvmToolchain(17)
    androidTarget()

    js(IR) {
        moduleName = "emarsys-sdk"
        browser {
            commonWebpackConfig {
                outputFileName = "emarsys-sdk.js"
            }
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
            binaries.executable()
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "EmarsysSDK"
            isStatic = false
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.okio)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.serialization)
                implementation(project.dependencies.platform(libs.cryptography))
                implementation(libs.cryptography.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.ktor.client.mock)
                implementation(libs.kotest.framework.engine)
                implementation(libs.kotest.assertions.core)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.android)
                implementation(libs.ktor.client.android)
                implementation(libs.androidx.core.ktx)
                implementation(libs.cryptography.provider.jdk)
                implementation(libs.startup.runtime)
                implementation(libs.androidx.lifecycle.common)
                implementation(libs.androidx.lifecycle.process)
                implementation(libs.androidx.appcompat)
                implementation(libs.sqlDelight.android)
            }
        }

        val androidUnitTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.mockk.android)
            }
        }
        val androidInstrumentedTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.mockk.android)
                implementation(libs.androidx.runner)
                implementation(libs.androidx.test.junit)
                implementation(libs.kotest.assertions.core)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.okio.fakefilesystem)
            }
        }
        iosMain {
            dependencies {
                implementation(libs.ktor.client.apple)
                implementation(libs.cryptography.provider.apple)
                implementation(libs.sqlDelight.native)
            }
        }
        iosTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(libs.ktor.client.js)
                implementation(libs.kotlin.wrapper.browser)
                implementation(libs.cryptography.provider.webcrypto)
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}

android {
    namespace = "com.emarsys"
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
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}

buildConfig {
    packageName("com.emarsys.core.device")
    buildConfigField("String", "VERSION_NAME", "\"4.0.0\"")
}

afterEvaluate {
    tasks.withType<AndroidLintAnalysisTask>()
        .configureEach {// https://github.com/gmazzo/gradle-buildconfig-plugin/issues/67
            mustRunAfter(tasks.withType<BuildConfigTask>())
        }
}

sqldelight {
    databases {
        create("EmarsysDB") {
            packageName.set("com.emarsys.sqldelight")
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
