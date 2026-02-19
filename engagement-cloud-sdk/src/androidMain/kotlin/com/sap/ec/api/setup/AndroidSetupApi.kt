package com.sap.ec.api.setup

import com.sap.ec.api.config.AndroidEngagementCloudSDKConfig

interface AndroidSetupApi {

    suspend fun enable(config: AndroidEngagementCloudSDKConfig): Result<Unit>

    suspend fun disable(): Result<Unit>

    suspend fun isEnabled(): Boolean
}