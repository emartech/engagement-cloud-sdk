package com.sap.ec.context

import com.sap.ec.api.SdkState
import com.sap.ec.config.SdkConfig
import com.sap.ec.core.log.LogLevel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.StateFlow

internal interface SdkContextApi {
    val currentSdkState: StateFlow<SdkState>
    val sdkDispatcher: CoroutineDispatcher
    val mainDispatcher: CoroutineDispatcher
    var contactFieldValue: String?
    var openIdToken: String?
    var config: SdkConfig?
    var defaultUrls: DefaultUrlsApi
    var remoteLogLevel: LogLevel
    val features: MutableSet<Features>
    var logBreadcrumbsQueueSize: Int

    suspend fun setSdkState(sdkState: SdkState)
}