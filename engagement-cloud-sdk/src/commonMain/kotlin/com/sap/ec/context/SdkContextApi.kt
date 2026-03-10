package com.sap.ec.context

import com.sap.ec.api.SdkState
import com.sap.ec.config.LinkContactData
import com.sap.ec.config.SdkConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.StateFlow

internal interface SdkContextApi {
    val currentSdkState: StateFlow<SdkState>
    val sdkDispatcher: CoroutineDispatcher
    val mainDispatcher: CoroutineDispatcher
    var config: SdkConfig?
    var onContactLinkingFailed: (suspend () -> LinkContactData?)?
    var defaultUrls: DefaultUrlsApi
    val features: MutableSet<Features>

    suspend fun isEnabledState(): Boolean

    suspend fun getSdkConfig(): SdkConfig?

    suspend fun setSdkConfig(config: SdkConfig?)

    suspend fun setSdkState(sdkState: SdkState)
}