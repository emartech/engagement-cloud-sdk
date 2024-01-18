package com.emarsys.core.networking.clients.event

import com.emarsys.core.networking.clients.event.model.Event

interface EventClientApi {

    suspend fun registerEvent(event: Event)
}