package com.sap.ec

@InternalSdkApi
enum class KotlinPlatform {
    JS,
    Android,
    IOS
}

@InternalSdkApi
expect val currentPlatform: KotlinPlatform