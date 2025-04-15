package com.emarsys.core.channel

import com.emarsys.networking.clients.event.model.SdkEvent

interface SdkEventWaiterApi {
    val sdkEventDistributor: SdkEventDistributor
    val sdkEvent: SdkEvent

    suspend fun await()
}