plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.googleServices)
}

dependencies {
    implementation(libs.fcm)
}
android {
    namespace = "com.emarsys.fcm"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
}
