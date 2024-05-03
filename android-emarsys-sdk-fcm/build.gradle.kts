plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.googleServices)
}

dependencies {
    implementation(libs.fcm)

    implementation(kotlin("test"))
    implementation(libs.mockk.android)
    implementation(libs.androidx.runner)
    androidTestImplementation(libs.junit)
    implementation(libs.kotest.assertions.core)
}
android {
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

    kotlinOptions {
        jvmTarget = "1.8"
    }
}