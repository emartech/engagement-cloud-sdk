plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.agconnect)
    alias(libs.plugins.builtInKotlin)
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
