package com.emarsys.core.channel

import com.emarsys.event.SdkEvent
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first

internal class SdkEventWaiter(
    override val sdkEventDistributor: SdkEventDistributorApi,
    override val sdkEvent: SdkEvent,
    override val connectionStatus: StateFlow<Boolean>
) : SdkEventWaiterApi {

    @Suppress("UNCHECKED_CAST")
    override suspend fun <T> await(): SdkEvent.Internal.Sdk.Answer.Response<T> {
        return if (connectionStatus.value) {
            sdkEventDistributor.sdkEventFlow.first { it is SdkEvent.Internal.Sdk.Answer && sdkEvent.id == it.originId } as SdkEvent.Internal.Sdk.Answer.Response<T>
        } else {
            SdkEvent.Internal.Sdk.Answer.Response(
                sdkEvent.id,
                Result.failure(Exception("No internet connection. Event: ${sdkEvent.type} is stored and will be processed when connection is restored."))
            )
        }
    }
}