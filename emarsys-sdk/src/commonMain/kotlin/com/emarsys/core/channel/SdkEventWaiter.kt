package com.emarsys.core.channel

import com.emarsys.event.SdkEvent
import kotlinx.coroutines.flow.first

internal class SdkEventWaiter(
    override val sdkEventDistributor: SdkEventDistributor,
    override val sdkEvent: SdkEvent
) : SdkEventWaiterApi {

    override suspend fun <T> await(): SdkEvent.Internal.Sdk.Answer.Response<T> {
        return sdkEventDistributor.sdkEventFlow.first { it is SdkEvent.Internal.Sdk.Answer && sdkEvent.id == it.originId } as SdkEvent.Internal.Sdk.Answer.Response<T>
    }
}