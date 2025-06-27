package com.emarsys.core.channel

import com.emarsys.event.OnlineSdkEvent
import com.emarsys.event.SdkEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

internal interface SdkEventDistributorApi {
    val sdkEventFlow: SharedFlow<SdkEvent>
    val onlineSdkEvents: Flow<OnlineSdkEvent>
    val logEvents: Flow<SdkEvent.Internal.LogEvent>

    suspend fun registerEvent(sdkEvent: SdkEvent): SdkEventWaiterApi?

}