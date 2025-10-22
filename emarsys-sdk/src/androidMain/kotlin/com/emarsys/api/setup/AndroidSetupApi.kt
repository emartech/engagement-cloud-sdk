package com.emarsys.api.setup

import com.emarsys.AndroidEmarsysConfig

interface AndroidSetupApi {

    suspend fun enableTracking(config: AndroidEmarsysConfig): Result<Unit>

    suspend fun disableTracking(): Result<Unit>

    suspend fun isEnabled(): Boolean
}