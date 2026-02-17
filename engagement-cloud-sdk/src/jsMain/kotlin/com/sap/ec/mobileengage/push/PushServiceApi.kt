package com.sap.ec.mobileengage.push

import JsEngagementCloudSDKConfig

interface PushServiceApi {
    suspend fun register(config: JsEngagementCloudSDKConfig)
    suspend fun subscribeForPushMessages(config: JsEngagementCloudSDKConfig)
}