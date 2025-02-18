package com.emarsys.mobileengage.action.actions

import com.emarsys.mobileengage.action.models.CustomEventActionModel
import com.emarsys.networking.clients.event.model.Event
import com.emarsys.networking.clients.event.model.EventType
import kotlinx.coroutines.flow.MutableSharedFlow

class CustomEventAction(
    private val action: CustomEventActionModel,
    private val sdkEventFlow: MutableSharedFlow<Event>
) : Action<Unit> {
    override suspend fun invoke(value: Unit?) {
        sdkEventFlow.emit(Event(EventType.CUSTOM, action.name, action.payload))
    }
}
