package com.emarsys.mobileengage.push

import com.emarsys.JsEmarsysConfig

interface PushServiceApi {
    suspend fun register(config: JsEmarsysConfig)
}