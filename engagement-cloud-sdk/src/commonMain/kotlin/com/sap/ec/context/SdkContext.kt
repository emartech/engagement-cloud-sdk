package com.sap.ec.context

import com.sap.ec.api.SdkState
import com.sap.ec.config.SdkConfig
import com.sap.ec.core.log.LogLevel
import com.sap.ec.core.storage.Store
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.builtins.serializer

internal class SdkContext(
    override val sdkDispatcher: CoroutineDispatcher,
    override val mainDispatcher: CoroutineDispatcher,
    override var defaultUrls: DefaultUrlsApi,
    override var remoteLogLevel: LogLevel,
    override val features: MutableSet<Features>,
    override var logBreadcrumbsQueueSize: Int,
) : SdkContextApi {

    override var contactFieldValue: String? by Store(serializer =  String.serializer(), key = "contactFieldValue")
    override var openIdToken: String? by Store(serializer =  String.serializer(), key = "openIdToken")

    override var config: SdkConfig? = null

    override val currentSdkState = MutableStateFlow(SdkState.UnInitialized)

    override suspend fun setSdkState(sdkState: SdkState) {
        currentSdkState.value = sdkState
    }
}

