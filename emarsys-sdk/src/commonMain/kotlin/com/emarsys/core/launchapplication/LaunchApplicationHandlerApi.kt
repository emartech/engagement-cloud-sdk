package com.emarsys.core.launchapplication

import com.emarsys.SdkConfig

interface LaunchApplicationHandlerApi {

    suspend fun launchApplication(config: SdkConfig)

}