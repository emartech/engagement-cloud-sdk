package com.sap.ec.core.providers.sdkversion

internal interface SdkVersionProviderApi {
    fun provide(): String
}