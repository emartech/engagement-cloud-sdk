package com.emarsys.api.setup

import com.emarsys.config.SdkConfig

interface SetupApi {

    suspend fun enableTracking(config: SdkConfig): Result<Unit>

    suspend fun disableTracking(): Result<Unit>
}