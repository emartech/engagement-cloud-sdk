plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.agconnect)
    alias(libs.plugins.builtInKotlin)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.composeCompiler)
}

android {
    enableKotlin = true
    namespace = "com.sap.ec.sample"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.sap.ec.sample"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            pickFirsts.add("META-INF/LICENSE.md")
            pickFirsts.add("META-INF/LICENSE-notice.md")
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation(project(":composeApp"))
    implementation(project(":engagement-cloud-sdk"))
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(project(":engagement-cloud-sdk-android-fcm"))
    implementation(project(":engagement-cloud-sdk-android-hms"))
    debugImplementation(libs.compose.ui.tooling)
}
