package com.sap.ec.context

import com.sap.ec.api.SdkState
import com.sap.ec.config.LinkContactData
import com.sap.ec.config.SdkConfig
import com.sap.ec.core.log.LogLevel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow

internal class SdkContext(
    override val sdkDispatcher: CoroutineDispatcher,
    override val mainDispatcher: CoroutineDispatcher,
    override var onContactLinkingFailed: (suspend () -> LinkContactData?)?,
    override var defaultUrls: DefaultUrlsApi,
    override var remoteLogLevel: LogLevel,
    override val features: MutableSet<Features>,
    override var logBreadcrumbsQueueSize: Int,
) : SdkContextApi {
    override var config: SdkConfig? = null

    override val currentSdkState = MutableStateFlow(SdkState.UnInitialized)

    override suspend fun setSdkState(sdkState: SdkState) {
        currentSdkState.value = sdkState
    }
}

