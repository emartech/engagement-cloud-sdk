package com.sap.ec.context

import com.sap.ec.api.SdkState
import com.sap.ec.config.LinkContactData
import com.sap.ec.config.SdkConfig
import com.sap.ec.enable.config.SdkConfigStoreApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow

internal class SdkContext(
    override val sdkDispatcher: CoroutineDispatcher,
    override val mainDispatcher: CoroutineDispatcher,
    override var onContactLinkingFailed: (suspend () -> LinkContactData?)?,
    override var defaultUrls: DefaultUrlsApi,
    override val features: MutableSet<Features>,
    private val sdkConfigStore: SdkConfigStoreApi<SdkConfig>
) : SdkContextApi {
    private var _cachedConfig: SdkConfig? = null

    override suspend fun getSdkConfig(): SdkConfig? {
        return _cachedConfig ?: sdkConfigStore.load().also { _cachedConfig = it }
    }

    override suspend fun setSdkConfig(config: SdkConfig?) {
        config?.let {
            sdkConfigStore.store(config)
            _cachedConfig = config
        } ?: sdkConfigStore.clear()
    }

    override val currentSdkState = MutableStateFlow(SdkState.UnInitialized)

    override suspend fun isEnabledState(): Boolean =
        currentSdkState.value == SdkState.OnHold || currentSdkState.value == SdkState.Active

    override suspend fun setSdkState(sdkState: SdkState) {
        currentSdkState.value = sdkState
    }
}

