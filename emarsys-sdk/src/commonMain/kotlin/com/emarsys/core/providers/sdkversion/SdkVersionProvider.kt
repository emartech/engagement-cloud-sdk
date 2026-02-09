package com.emarsys.core.providers.sdkversion

import com.emarsys.core.device.BuildConfig

class SdkVersionProvider: SdkVersionProviderApi {
    override fun provide(): String {
        return BuildConfig.VERSION_NAME
    }
}