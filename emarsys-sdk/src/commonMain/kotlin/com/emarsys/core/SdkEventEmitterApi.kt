package com.emarsys.core

import com.emarsys.networking.clients.event.model.SdkEvent

interface SdkEventEmitterApi {

    suspend fun emitEvent(sdkEvent: SdkEvent)

}