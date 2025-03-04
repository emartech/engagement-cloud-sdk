package com.emarsys.context

import com.emarsys.SdkConfig
import com.emarsys.api.SdkState
import com.emarsys.core.Observable
import com.emarsys.core.log.LogLevel
import kotlinx.coroutines.CoroutineDispatcher

class SdkContext(
    override val sdkDispatcher: CoroutineDispatcher,
    override val mainDispatcher: CoroutineDispatcher,
    override var defaultUrls: DefaultUrlsApi,
    override var remoteLogLevel: LogLevel,
    override val features: MutableSet<Features>,
) : SdkContextApi, Observable<SdkState>(SdkState.inactive) {

    override var contactFieldId: Int? = null

    override var config: SdkConfig? = null

    override val currentSdkState
        get() = this.value

    override suspend fun setSdkState(sdkState: SdkState) {
        changeValue(sdkState)
    }
}

fun SdkContextApi.isConfigPredictOnly(): Boolean {
    return this.config?.applicationCode == null && this.config?.merchantId != null
}

