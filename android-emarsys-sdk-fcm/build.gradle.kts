plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.builtInKotlin)
}

dependencies {
    api(libs.fcm)

    testImplementation(libs.kotlin.test)
    implementation(libs.mockk.android)
    implementation(libs.androidx.runner)
    implementation(libs.kotest.assertions.core)
    implementation(libs.kotlinx.serialization.json)

    androidTestImplementation(libs.junit)
}
android {
    enableKotlin = true
    namespace = "com.emarsys.fcm"
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