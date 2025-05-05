package com.emarsys.core.channel

import com.emarsys.networking.clients.event.model.SdkEvent

internal interface SdkEventWaiterApi {
    val sdkEventDistributor: SdkEventDistributor
    val sdkEvent: SdkEvent

    suspend fun await()
}