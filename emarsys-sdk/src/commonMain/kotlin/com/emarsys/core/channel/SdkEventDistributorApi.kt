package com.emarsys.core.channel

import com.emarsys.networking.clients.event.model.SdkEvent

interface SdkEventDistributorApi {

    suspend fun registerEvent(sdkEvent: SdkEvent)

}