package com.emarsys.core.channel

import com.emarsys.event.SdkEvent

internal interface SdkEventWaiterApi {
    val sdkEventDistributor: SdkEventDistributor
    val sdkEvent: SdkEvent

    suspend fun await()
}