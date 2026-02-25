package com.sap.ec.mobileengage.inapp.jsbridge

import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.providers.sdkversion.SdkVersionProviderApi

internal class ContentReplacer(
    private val sdkContext: SdkContextApi,
    private val sdkVersionProvider: SdkVersionProviderApi,
    private val jsBridgeVerifier: JsBridgeVerifierApi
) : ContentReplacerApi {
    private companion object {
        const val JS_BRIDGE_PLACEHOLDER = "<!-- EC-JS-BRIDGE-SCRIPT -->"
        const val SDK_VERSION_PLACEHOLDER = "EC-SDK-VERSION"
    }

    override suspend fun replace(content: String): String {
        val shouldInject = jsBridgeVerifier.shouldInjectJsBridge().getOrDefault(false)
        val jsBridgeTag = if (shouldInject) {
            "<script src=\"${sdkContext.defaultUrls.jsBridgeUrl}\"></script>"
        } else {
            ""
        }
        return content
            .replace(JS_BRIDGE_PLACEHOLDER, jsBridgeTag)
            .replace(SDK_VERSION_PLACEHOLDER, sdkVersionProvider.provide())
    }
}
