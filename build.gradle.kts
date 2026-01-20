buildscript{
    repositories {
        maven(url = "https://developer.huawei.com/repo/")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:9.0.0")
        classpath("com.huawei.agconnect:agcp:1.9.3.301")
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