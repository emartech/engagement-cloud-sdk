package com.sap.ec.core.channel

import com.sap.ec.InternalSdkApi
import com.sap.ec.event.SdkEvent
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration

@InternalSdkApi
interface SdkEventWaiterApi {
    val sdkEventDistributor: SdkEventDistributorApi
    val sdkEvent: SdkEvent

    val connectionStatus: StateFlow<Boolean>

    suspend fun <T> await(): SdkEvent.Internal.Sdk.Answer.Response<T>
    suspend fun <T> await(timeout: Duration): SdkEvent.Internal.Sdk.Answer.Response<T>
}