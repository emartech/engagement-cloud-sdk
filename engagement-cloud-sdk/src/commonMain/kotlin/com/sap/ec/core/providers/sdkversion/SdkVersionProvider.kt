package com.sap.ec.core.providers.sdkversion

import com.sap.ec.core.device.BuildConfig

class SdkVersionProvider: SdkVersionProviderApi {
    override fun provide(): String {
        return BuildConfig.VERSION_NAME
    }
}