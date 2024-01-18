package com.emarsys.core.channel

import com.emarsys.core.networking.clients.event.model.Event
import kotlinx.coroutines.flow.Flow

interface DeviceEventChannelApi {

    suspend fun send(event: Event)

    suspend fun consume(): Flow<Event>
}