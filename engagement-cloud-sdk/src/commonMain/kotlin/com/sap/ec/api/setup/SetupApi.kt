package com.sap.ec.api.setup

import com.sap.ec.config.SdkConfig

interface SetupApi {

    suspend fun enableTracking(config: SdkConfig): Result<Unit>

    suspend fun disableTracking(): Result<Unit>

    suspend fun isEnabled(): Boolean

}