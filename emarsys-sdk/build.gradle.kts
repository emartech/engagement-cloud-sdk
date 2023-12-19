import com.android.build.gradle.internal.lint.AndroidLintAnalysisTask
import com.github.gmazzo.gradle.plugins.BuildConfigTask

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.dokka)
    alias(libs.plugins.mockmp)
    alias(libs.plugins.ksp)
    alias(libs.plugins.buildConfig)
}

group = "com.emarsys"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = "https://www.jitpack.io")
    google()

}

kotlin {
    //explicitApi()
    jvmToolchain(17)
    androidTarget()

    js {
        moduleName = "emarsys-sdk"
        browser {
            commonWebpackConfig {
                outputFileName = "emarsys-sdk.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
            implementation("io.ktor:ktor-client-core:2.3.4")
            implementation("io.ktor:ktor-client-content-negotiation:2.3.4")
            implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.4")
            implementation("io.ktor:ktor-serialization:2.3.4")
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation("io.ktor:ktor-client-mock:2.3.4")
            implementation("io.kotest:kotest-framework-engine:5.8.0")
            implementation("io.kotest:kotest-assertions-core:5.8.0")
        }
        androidMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
            implementation("io.ktor:ktor-client-android:2.3.4")
        }

        val androidUnitTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidInstrumentedTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("io.mockk:mockk-android:1.13.8")
                implementation("androidx.test:runner:1.5.2")
                implementation("androidx.test.ext:junit:1.1.5")
                implementation("io.kotest:kotest-assertions-core:5.8.0")
            }
        }
        jsMain.dependencies {
            implementation("io.ktor:ktor-client-js:2.3.4")
        }
        jsTest.dependencies {
            implementation(kotlin("test-js"))
        }
    }
}

android {
    namespace = "com.emarsys"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/LICENSE.md"
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
    buildConfigField("String", "VERSION_NAME", "\"0.0.1\"")
}

afterEvaluate {
    tasks.withType<AndroidLintAnalysisTask>()
        .configureEach {// https://github.com/gmazzo/gradle-buildconfig-plugin/issues/67
            mustRunAfter(tasks.withType<BuildConfigTask>())
        }
}

mockmp {
    usesHelper = true
    installWorkaround()
}
