plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.mockmp) apply false
    alias(libs.plugins.ksp) apply false
}

repositories {
    google()
    mavenCentral()
    maven(url = "https://www.jitpack.io")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}