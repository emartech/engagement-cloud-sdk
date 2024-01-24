package com.emarsys.context

import com.emarsys.EmarsysConfig
import com.emarsys.api.SdkState
import com.emarsys.core.log.LogLevel
import com.emarsys.core.ObservableApi
import com.emarsys.core.Observer
import kotlinx.coroutines.CoroutineDispatcher

interface SdkContextApi: ObservableApi<SdkState> {
    val currentSdkState: SdkState
    val sdkDispatcher: CoroutineDispatcher
    var config: EmarsysConfig?
    var inAppDndD: Boolean
    var defaultUrls: DefaultUrlsApi
    var remoteLogLevel: LogLevel
    val features: MutableSet<Features>

    suspend fun setSdkState(sdkState: SdkState)

    override fun addObserver(observer: Observer<SdkState>)

    override fun removeObserver(observer: Observer<SdkState>)
}