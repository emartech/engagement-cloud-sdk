package com.sap.ec.core.channel

import com.sap.ec.InternalSdkApi
import com.sap.ec.event.OnlineSdkEvent
import com.sap.ec.api.event.model.EngagementCloudEvent
import com.sap.ec.event.SdkEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

@InternalSdkApi
interface SdkEventDistributorApi {
    val sdkEventFlow: SharedFlow<SdkEvent>
    val sdkPublicEventFlow: SharedFlow<EngagementCloudEvent>
    val onlineSdkEvents: Flow<OnlineSdkEvent>
    val logEvents: Flow<SdkEvent.Internal.LogEvent>

    /**
     * Registers an [SdkEvent] by persisting [OnlineSdkEvent] instances to the database and
     * emitting the event to the SDK event flow. Returns an [SdkEventWaiterApi] that can be
     * used to await a response event matching the original event's id.
     *
     * Events implementing [OnlineSdkEvent] are stored in the database so they survive app
     * restarts and can be retried. Non-[OnlineSdkEvent] events are emitted to the flow
     * without persistence.
     *
     * For fire-and-forget local signals that do not need persistence or a response waiter,
     * use [SdkEventEmitterApi.emitEvent] instead.
     */
    suspend fun registerEvent(sdkEvent: SdkEvent): SdkEventWaiterApi
    suspend fun registerPublicEvent(sdkEvent: EngagementCloudEvent)

}