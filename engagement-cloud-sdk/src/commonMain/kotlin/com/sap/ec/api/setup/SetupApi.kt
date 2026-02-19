package com.sap.ec.api.setup

import com.sap.ec.config.SdkConfig

interface SetupApi {

    suspend fun enable(config: SdkConfig): Result<Unit>

    suspend fun disable(): Result<Unit>

    suspend fun isEnabled(): Boolean

}