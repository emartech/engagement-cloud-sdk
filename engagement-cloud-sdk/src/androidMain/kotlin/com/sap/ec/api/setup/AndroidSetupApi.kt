package com.sap.ec.api.setup

import com.sap.ec.api.config.AndroidEngagementCloudSDKConfig

interface AndroidSetupApi {

    suspend fun enableTracking(config: AndroidEngagementCloudSDKConfig): Result<Unit>

    suspend fun disableTracking(): Result<Unit>

    suspend fun isEnabled(): Boolean
}