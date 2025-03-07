package com.emarsys.core.db

import com.emarsys.networking.clients.event.model.SdkEvent

interface EventsDaoApi {
    fun insertEvent(event: SdkEvent)
    fun getEvents(): List<SdkEvent>
}