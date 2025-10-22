package com.emarsys.context

import com.emarsys.api.SdkState
import com.emarsys.config.SdkConfig
import com.emarsys.core.log.LogLevel
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
    var embeddedMessagingFrequencyCapSeconds : Int

    suspend fun setSdkState(sdkState: SdkState)
}