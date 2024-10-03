package com.emarsys.mobileengage.action.actions

import com.emarsys.core.channel.CustomEventChannelApi
import com.emarsys.mobileengage.action.models.CustomEventActionModel
import com.emarsys.networking.clients.event.model.Event
import com.emarsys.networking.clients.event.model.EventType

class CustomEventAction(
    private val action: CustomEventActionModel,
    private val eventChannel: CustomEventChannelApi
): Action<Unit> {
    override suspend fun invoke(value: Unit?) {
        eventChannel.send(Event(EventType.CUSTOM, action.name, action.payload))
    }
}
