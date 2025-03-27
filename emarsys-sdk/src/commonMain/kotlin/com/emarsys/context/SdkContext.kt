package com.emarsys.context

import com.emarsys.SdkConfig
import com.emarsys.api.SdkState
import com.emarsys.core.log.LogLevel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow

class SdkContext(
    override val sdkDispatcher: CoroutineDispatcher,
    override val mainDispatcher: CoroutineDispatcher,
    override var defaultUrls: DefaultUrlsApi,
    override var remoteLogLevel: LogLevel,
    override val features: MutableSet<Features>,
) : SdkContextApi {

    override var contactFieldId: Int? = null

    override var config: SdkConfig? = null

    override val currentSdkState = MutableStateFlow(SdkState.inactive)

    override suspend fun setSdkState(sdkState: SdkState) {
        currentSdkState.value = sdkState
    }
}

fun SdkContextApi.isConfigPredictOnly(): Boolean {
    return this.config?.applicationCode == null && this.config?.merchantId != null
}

