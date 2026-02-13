package com.emarsys.mobileengage.inapp.jsbridge

import com.emarsys.context.SdkContextApi
import com.emarsys.core.providers.sdkversion.SdkVersionProviderApi

internal class ContentReplacer(
    private val sdkContext: SdkContextApi,
    private val sdkVersionProvider: SdkVersionProviderApi
) : ContentReplacerApi {
    private companion object {
        const val JS_BRIDGE_PLACEHOLDER = "<!-- EC-JS-BRIDGE-SCRIPT -->"
        const val SDK_VERSION_PLACEHOLDER = "EC-SDK-VERSION"
    }

    override fun replace(content: String): String {
        return content
            .replace(JS_BRIDGE_PLACEHOLDER, sdkContext.defaultUrls.ecJsBridgeUrl)
            .replace(SDK_VERSION_PLACEHOLDER, sdkVersionProvider.provide())
    }
}