package com.emarsys.core.channel

import com.emarsys.networking.clients.event.model.SdkEvent
import kotlinx.coroutines.flow.first

internal class SdkEventWaiter(
    override val sdkEventDistributor: SdkEventDistributor,
    override val sdkEvent: SdkEvent
) : SdkEventWaiterApi {

    override suspend fun await() {
        sdkEventDistributor.sdkEventFlow.first { it is SdkEvent.Internal.Sdk.Answer && sdkEvent.id == it.originId }
    }
}