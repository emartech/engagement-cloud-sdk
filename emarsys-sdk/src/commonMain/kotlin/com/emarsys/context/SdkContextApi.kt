package com.emarsys.context

import com.emarsys.EmarsysConfig
import com.emarsys.api.SdkState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.StateFlow

interface SdkContextApi {
    val sdkState: StateFlow<SdkState>
    val sdkDispatcher: CoroutineDispatcher
    var config: EmarsysConfig?
    var inAppDndD: Boolean
    fun setSdkState(sdkState: SdkState)
}