package com.sap.ec.core.channel

import com.sap.ec.event.SdkEvent

interface SdkEventEmitterApi {

    suspend fun emitEvent(sdkEvent: SdkEvent)

}