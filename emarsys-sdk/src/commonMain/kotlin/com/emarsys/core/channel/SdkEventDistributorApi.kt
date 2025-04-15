package com.emarsys.core.channel

import com.emarsys.networking.clients.event.model.OnlineSdkEvent
import com.emarsys.networking.clients.event.model.SdkEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

interface SdkEventDistributorApi {
    val sdkEventFlow: SharedFlow<SdkEvent>
    val onlineSdkEvents: Flow<OnlineSdkEvent>
    val logEvents: Flow<SdkEvent.Internal.LogEvent>

    suspend fun registerEvent(sdkEvent: SdkEvent): SdkEventWaiterApi?

}