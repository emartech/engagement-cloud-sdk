package com.emarsys.context

import com.emarsys.SdkConfig
import com.emarsys.api.SdkState
import com.emarsys.core.log.LogLevel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.StateFlow

interface SdkContextApi {
    val currentSdkState: StateFlow<SdkState>
    val sdkDispatcher: CoroutineDispatcher
    val mainDispatcher: CoroutineDispatcher
    var contactFieldId: Int?
    var config: SdkConfig?
    var defaultUrls: DefaultUrlsApi
    var remoteLogLevel: LogLevel
    val features: MutableSet<Features>

    suspend fun setSdkState(sdkState: SdkState)
}