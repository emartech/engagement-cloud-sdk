package com.emarsys

enum class KotlinPlatform {
    JS,
    Android,
    IOS
}

expect val currentPlatform: KotlinPlatform