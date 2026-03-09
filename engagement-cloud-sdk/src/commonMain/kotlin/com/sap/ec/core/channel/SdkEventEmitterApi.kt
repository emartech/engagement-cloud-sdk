package com.sap.ec.core.channel

import com.sap.ec.InternalSdkApi
import com.sap.ec.event.SdkEvent

@InternalSdkApi
interface SdkEventEmitterApi {

    /**
     * Emits an [SdkEvent] directly to the SDK event flow without persisting it to the database.
     *
     * Use this method for local signaling events that do not need to survive app restarts
     * or be retried on failure (e.g., [SdkEvent.Internal.EmbeddedMessaging.TriggerRefresh]).
     *
     * For events that need persistence and retry semantics, use [SdkEventDistributorApi.registerEvent] instead.
     */
    suspend fun emitEvent(sdkEvent: SdkEvent)

}