package com.sap.ec.enable

import JsEngagementCloudSDKConfig
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.state.State
import com.sap.ec.mobileengage.push.PushServiceApi
import com.sap.ec.util.runCatchingWithoutCancellation

internal class PlatformInitState(
    private val pushService: PushServiceApi,
    val sdkContext: SdkContextApi,
) : State {

    override val name: String = "jsInitState"

    override fun prepare() {

    }

    override suspend fun active(): Result<Unit> {
        return runCatchingWithoutCancellation {
            sdkContext.config?.let {
                val jsEngagementCloudSDKConfig = sdkContext.config as JsEngagementCloudSDKConfig
                pushService.register(jsEngagementCloudSDKConfig)
                pushService.subscribeForPushMessages(jsEngagementCloudSDKConfig)
            }
        }
    }

    override fun relax() {
    }
}