package com.emarsys.networking.clients.event

import com.emarsys.networking.clients.event.model.Event


interface EventClientApi {

    suspend fun registerEvent(event: Event)
}