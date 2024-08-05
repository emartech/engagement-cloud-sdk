package com.emarsys.mobileengage.push

import com.emarsys.EmarsysConfig

interface PushServiceApi {
    suspend fun register(config: EmarsysConfig)
}