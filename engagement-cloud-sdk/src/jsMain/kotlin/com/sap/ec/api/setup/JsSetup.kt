package com.sap.ec.api.setup

import JsEngagementCloudSDKConfig
import com.sap.ec.JsApiConfig
import kotlinx.coroutines.await
import kotlin.js.Promise

internal class JsSetup(
    private val setup: SetupApi
) : JsSetupApi {

    @OptIn(ExperimentalWasmJsInterop::class)
    override suspend fun enable(
        config: JsApiConfig,
        onContactLinkingFailed: () -> Promise<JsLinkContactData?>
    ) {
        setup.enable(
            JsEngagementCloudSDKConfig(
                applicationCode = config.applicationCode,
                config.serviceWorkerOptions
            ),
            onContactLinkingFailed = {
                onContactLinkingFailed().await()?.toLinkContactData()
            })
    }

    override suspend fun disable() {
        setup.disable().onFailure { println(it.message) }
    }

    override suspend fun isEnabled(): Boolean {
        return setup.isEnabled()
    }

    override fun setOnContactLinkingFailedCallback(onContactLinkingFailed: () -> Promise<JsLinkContactData?>) {
        setup.setOnContactLinkingFailedCallback {
            onContactLinkingFailed().await()?.toLinkContactData()
        }
    }

}