buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:${property("agpVersion")}")
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
    alias(libs.plugins.mavenPublish) apply false
    alias(libs.plugins.dotenv)
}

val sdkVersion = System.getenv("VERSION_OVERRIDE") ?: "4.0.0-LOCAL"

allprojects {
	extra["SDK_VERSION"] = sdkVersion
}