package com.emarsys

enum class KotlinPlatform {
    JS,
    Android
}

expect val currentPlatform: KotlinPlatform