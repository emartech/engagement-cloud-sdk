package com.sap.ec

enum class KotlinPlatform {
    JS,
    Android,
    IOS
}

expect val currentPlatform: KotlinPlatform