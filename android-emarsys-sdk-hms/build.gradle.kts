plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.agconnect)
}

dependencies {
    implementation(libs.hms)
    implementation(libs.agconnect.core)
}
android {
    namespace = "com.emarsys.hms"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}
