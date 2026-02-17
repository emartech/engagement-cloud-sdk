package com.sap.ec.mobileengage.inapp.jsbridge

import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.providers.sdkversion.SdkVersionProviderApi

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