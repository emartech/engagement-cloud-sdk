package com.emarsys.core.channel

import com.emarsys.core.networking.clients.event.model.Event
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow

class DeviceEventChannel(
    private val eventChannel: Channel<Event>,
) : DeviceEventChannelApi {

    override suspend fun send(event: Event) {
        eventChannel.send(event)
    }

    override suspend fun consume(): Flow<Event> {
        return eventChannel.consumeAsFlow()
    }
}