package com.sap.ec.api.setup

import com.sap.ec.IosEngagementCloudSDKConfig
import com.sap.ec.core.exceptions.SdkException.SdkAlreadyDisabledException
import com.sap.ec.core.exceptions.SdkException.SdkAlreadyEnabledException
import io.ktor.utils.io.CancellationException

interface IosSetupApi {

    @Throws(SdkAlreadyEnabledException::class, CancellationException::class)
    suspend fun enableTracking(config: IosEngagementCloudSDKConfig)

    @Throws(SdkAlreadyDisabledException::class, CancellationException::class)
    suspend fun disableTracking()

    suspend fun isEnabled(): Boolean
}