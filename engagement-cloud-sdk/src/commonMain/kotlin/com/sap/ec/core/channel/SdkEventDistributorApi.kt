package com.sap.ec.core.channel

import com.sap.ec.event.OnlineSdkEvent
import com.sap.ec.event.SdkEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

internal interface SdkEventDistributorApi {
    val sdkEventFlow: SharedFlow<SdkEvent>
    val onlineSdkEvents: Flow<OnlineSdkEvent>
    val logEvents: Flow<SdkEvent.Internal.LogEvent>

    suspend fun registerEvent(sdkEvent: SdkEvent): SdkEventWaiterApi

}