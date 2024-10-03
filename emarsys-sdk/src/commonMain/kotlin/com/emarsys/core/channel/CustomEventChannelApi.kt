package com.emarsys.core.channel

import com.emarsys.networking.clients.event.model.Event
import kotlinx.coroutines.flow.Flow

interface CustomEventChannelApi {

    suspend fun send(event: Event)

    suspend fun consume(): Flow<Event>
}