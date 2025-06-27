package com.emarsys.core.channel

import com.emarsys.event.SdkEvent

interface SdkEventEmitterApi {

    suspend fun emitEvent(sdkEvent: SdkEvent)

}