package com.emarsys.mobileengage.action.actions

import com.emarsys.mobileengage.action.models.AppEventActionModel
import com.emarsys.mobileengage.events.SdkEventSource
import com.emarsys.networking.clients.event.model.Event
import com.emarsys.networking.clients.event.model.EventType
import kotlinx.coroutines.flow.MutableSharedFlow

class AppEventAction(
    private val action: AppEventActionModel,
    private val sdkEvents: MutableSharedFlow<Event>
) : Action<SdkEventSource> {
    override suspend fun invoke(value: SdkEventSource?) {
        value?.let {
            sdkEvents.emit(Event(EventType.CUSTOM, action.name, action.payload))
        }
    }
}