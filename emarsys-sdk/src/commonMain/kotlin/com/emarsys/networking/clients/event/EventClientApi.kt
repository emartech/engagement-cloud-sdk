package com.emarsys.networking.clients.event

import com.emarsys.networking.clients.event.model.SdkEvent


interface EventClientApi {

    suspend fun registerEvent(event: SdkEvent)
}