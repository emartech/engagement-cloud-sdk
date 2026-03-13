package com.sap.ec.mobileengage.inapp.jsbridge

import com.sap.ec.core.providers.sdkversion.SdkVersionProviderApi
import com.sap.ec.core.storage.StorageConstants
import com.sap.ec.core.storage.StringStorageApi

internal class ContentReplacer(
    private val sdkVersionProvider: SdkVersionProviderApi,
    private val jsBridgeVerifier: JsBridgeVerifierApi,
    private val stringStorage: StringStorageApi
) : ContentReplacerApi {
    private companion object {
        const val JS_BRIDGE_PLACEHOLDER = "<!-- EC-JS-BRIDGE-SCRIPT -->"
        const val SDK_VERSION_PLACEHOLDER = "EC-SDK-VERSION"
    }

    override suspend fun replace(content: String): String {
        jsBridgeVerifier.verifyJsBridge()
        val jsBridge = stringStorage.get(StorageConstants.JS_BRIDGE)?.let {
            """<script type="text/javascript">$it</script>"""
        } ?: ""

        return content
            .replace(SDK_VERSION_PLACEHOLDER, sdkVersionProvider.provide())
            .replace(JS_BRIDGE_PLACEHOLDER, jsBridge)
    }
}
