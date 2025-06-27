plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.agconnect)
}

dependencies {
    api(libs.hms)
    api(libs.agconnect.core)

    implementation(kotlin("test"))
    implementation(libs.mockk.android)
    implementation(libs.androidx.runner)
    implementation(libs.kotest.assertions.core)

    androidTestImplementation(libs.kotlinx.serialization.json)
    androidTestImplementation(libs.junit)
}
android {
    namespace = "com.emarsys.hms"
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

    kotlinOptions {
        jvmTarget = "1.8"
    }
}
