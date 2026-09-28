package com.sap.ec.core.channel

import com.sap.ec.event.SdkEvent.Internal.OperationalEvent

interface OperationalEventDistributorApi {

    suspend fun registerEvent(sdkEvent: OperationalEvent): SdkEventWaiterApi
}