package com.emarsys.context

import com.emarsys.SdkConfig
import com.emarsys.api.SdkState
import com.emarsys.core.log.LogLevel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.StateFlow

internal interface SdkContextApi {
    val currentSdkState: StateFlow<SdkState>
    val sdkDispatcher: CoroutineDispatcher
    val mainDispatcher: CoroutineDispatcher
    var contactFieldId: Int?
    var config: SdkConfig?
    var defaultUrls: DefaultUrlsApi
    var remoteLogLevel: LogLevel
    val features: MutableSet<Features>
    var logBreadcrumbsQueueSize: Int

    suspend fun setSdkState(sdkState: SdkState)

    fun isConfigPredictOnly(): Boolean {
        return this.config?.applicationCode == null && this.config?.merchantId != null
    }
}