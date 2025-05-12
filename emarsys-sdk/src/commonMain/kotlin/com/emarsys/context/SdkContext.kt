package com.emarsys.context

import com.emarsys.SdkConfig
import com.emarsys.api.SdkState
import com.emarsys.core.log.LogLevel
import com.emarsys.core.storage.Store
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.builtins.serializer

class SdkContext(
    override val sdkDispatcher: CoroutineDispatcher,
    override val mainDispatcher: CoroutineDispatcher,
    override var defaultUrls: DefaultUrlsApi,
    override var remoteLogLevel: LogLevel,
    override val features: MutableSet<Features>,
    override var logBreadcrumbsQueueSize: Int,
) : SdkContextApi {

    override var contactFieldId: Int? by Store(serializer =  Int.serializer(), key = "contactFieldId")
    override var contactFieldValue: String? by Store(serializer =  String.serializer(), key = "contactFieldValue")
    override var openIdToken: String? by Store(serializer =  String.serializer(), key = "openIdToken")

    override var config: SdkConfig? = null

    override val currentSdkState = MutableStateFlow(SdkState.inactive)

    override suspend fun setSdkState(sdkState: SdkState) {
        currentSdkState.value = sdkState
    }
}

