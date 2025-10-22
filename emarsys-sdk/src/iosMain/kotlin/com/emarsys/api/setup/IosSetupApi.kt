package com.emarsys.api.setup

import com.emarsys.IosEmarsysConfig
import com.emarsys.core.exceptions.SdkException.SdkAlreadyDisabledException
import com.emarsys.core.exceptions.SdkException.SdkAlreadyEnabledException
import io.ktor.utils.io.CancellationException

interface IosSetupApi {

    @Throws(SdkAlreadyEnabledException::class, CancellationException::class)
    suspend fun enableTracking(config: IosEmarsysConfig)

    @Throws(SdkAlreadyDisabledException::class, CancellationException::class)
    suspend fun disableTracking()

    suspend fun isEnabled(): Boolean
}