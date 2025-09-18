package com.emarsys.api.setup

import com.emarsys.IosEmarsysConfig
import io.ktor.utils.io.CancellationException

interface IosSetupApi {

    @Throws(CancellationException::class)
    suspend fun enableTracking(config: IosEmarsysConfig)

    @Throws(CancellationException::class)
    suspend fun disableTracking()
}