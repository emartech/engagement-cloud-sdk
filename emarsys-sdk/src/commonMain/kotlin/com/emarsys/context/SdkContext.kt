package com.emarsys.context

import com.emarsys.EmarsysConfig
import com.emarsys.api.SdkState
import com.emarsys.core.log.LogLevel
import com.emarsys.core.Observable
import kotlinx.coroutines.CoroutineDispatcher

class SdkContext(
    override val sdkDispatcher: CoroutineDispatcher,
    override var defaultUrls: DefaultUrlsApi,
    override var remoteLogLevel: LogLevel,
    override val features: MutableSet<Features>
    ): SdkContextApi, Observable<SdkState>(SdkState.inactive) {

    override var config: EmarsysConfig? = null

    override var inAppDndD: Boolean = false

    override val currentSdkState
        get() = this.value

    override suspend fun setSdkState(sdkState: SdkState) {
        changeValue(sdkState)
    }
}

