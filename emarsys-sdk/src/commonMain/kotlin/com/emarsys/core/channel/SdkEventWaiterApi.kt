package com.emarsys.core.channel

import com.emarsys.event.SdkEvent
import kotlinx.coroutines.flow.StateFlow

internal interface SdkEventWaiterApi {
    val sdkEventDistributor: SdkEventDistributorApi
    val sdkEvent: SdkEvent

    val connectionStatus: StateFlow<Boolean>

    suspend fun <T>await() : SdkEvent.Internal.Sdk.Answer.Response<T>
}