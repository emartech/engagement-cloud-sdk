package com.emarsys.core.channel

import com.emarsys.networking.clients.event.model.SdkEvent

interface SdkEventEmitterApi {

    suspend fun emitEvent(sdkEvent: SdkEvent)

}