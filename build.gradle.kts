buildscript {
    dependencies {
        // Required: the Huawei agconnect plugin (AGCPlugin.groovy) uses
        // GradleVersionTool to read the AGP version from the buildscript classpath.
        // Without this entry, plugin application fails with "No value present".
        // Keep this in sync with the 'agp' version in gradle/libs.versions.toml.
        classpath("com.android.tools.build:gradle:9.1.0")
    }
}

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinMultiplatformLibrary) apply false
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.buildConfig) apply false
    alias(libs.plugins.googleServices) apply false
    alias(libs.plugins.agconnect) apply false
    alias(libs.plugins.dotenv)
}