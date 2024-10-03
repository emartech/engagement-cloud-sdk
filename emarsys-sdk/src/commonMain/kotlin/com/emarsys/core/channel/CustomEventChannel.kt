package com.emarsys.core.channel

import com.emarsys.networking.clients.event.model.Event
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow

class CustomEventChannel(
    private val eventChannel: Channel<Event>,
) : CustomEventChannelApi {

    override suspend fun send(event: Event) {
        eventChannel.send(event)
    }

    override suspend fun consume(): Flow<Event> {
        return eventChannel.consumeAsFlow()
    }
}