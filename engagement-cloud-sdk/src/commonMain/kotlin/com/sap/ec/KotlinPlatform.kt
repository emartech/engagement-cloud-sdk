package com.sap.ec


internal enum class KotlinPlatform {
    JS,
    Android,
    IOS
}

internal expect val currentPlatform: KotlinPlatform