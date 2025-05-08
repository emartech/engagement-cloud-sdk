package com.emarsys.mobileengage.push

import JsEmarsysConfig

interface PushServiceApi {
    suspend fun register(config: JsEmarsysConfig)
    suspend fun subscribeForPushMessages(config: JsEmarsysConfig)
}