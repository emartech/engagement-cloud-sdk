package com.emarsys.context

import com.emarsys.EmarsysConfig
import com.emarsys.api.SdkState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SdkContext(override val sdkDispatcher: CoroutineDispatcher) : SdkContextApi {

    private val innerSdkState: MutableStateFlow<SdkState> = MutableStateFlow(SdkState.inactive)

    override val sdkState: StateFlow<SdkState> = innerSdkState.asStateFlow()

    override var config: EmarsysConfig? = null

    override var inAppDndD: Boolean = false

    override fun setSdkState(sdkState: SdkState) {
        innerSdkState.value = sdkState
    }
}

