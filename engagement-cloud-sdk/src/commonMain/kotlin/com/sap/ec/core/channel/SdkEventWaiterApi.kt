package com.sap.ec.core.channel

import com.sap.ec.InternalSdkApi
import com.sap.ec.event.SdkEvent
import kotlinx.coroutines.flow.StateFlow

@InternalSdkApi
interface SdkEventWaiterApi {
    val sdkEventDistributor: SdkEventDistributorApi
    val sdkEvent: SdkEvent

    val connectionStatus: StateFlow<Boolean>

    suspend fun <T> await(): SdkEvent.Internal.Sdk.Answer.Response<T>
}