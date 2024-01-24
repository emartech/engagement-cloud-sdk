package com.emarsys.context

import com.emarsys.EmarsysConfig
import com.emarsys.api.SdkState
import com.emarsys.core.ObservableApi
import com.emarsys.core.Observer
import kotlinx.coroutines.CoroutineDispatcher

interface SdkContextApi: ObservableApi<SdkState> {
    val currentSdkState: SdkState
    val sdkDispatcher: CoroutineDispatcher
    var config: EmarsysConfig?
    var inAppDndD: Boolean

    suspend fun setSdkState(sdkState: SdkState)
    override fun addObserver(observer: Observer<SdkState>)
    override fun removeObserver(observer: Observer<SdkState>)
}