package com.emarsys.core.channel

import com.emarsys.event.SdkEvent
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlin.reflect.KClass

internal class SdkEventWaiter(
    override val sdkEventDistributor: SdkEventDistributorApi,
    override val sdkEvent: SdkEvent,
    override val connectionStatus: StateFlow<Boolean>
) : SdkEventWaiterApi {

    @Suppress("UNCHECKED_CAST")
    override suspend fun <T : Any> await(expectedResultSuccessClass: KClass<T>?): SdkEvent.Internal.Sdk.Answer.Response<T> {
        return if (connectionStatus.value) {
            sdkEventDistributor.sdkEventFlow.first { event ->
                event is SdkEvent.Internal.Sdk.Answer.Response<*> &&
                        sdkEvent.id == event.originId &&
                        (event.result.isFailure || expectedResultSuccessClass?.isInstance(event.result.getOrNull()) != false)
            } as SdkEvent.Internal.Sdk.Answer.Response<T>
        } else {
            SdkEvent.Internal.Sdk.Answer.Response(
                sdkEvent.id,
                Result.failure(Exception("No internet connection. Event: ${sdkEvent.type} is stored and will be processed when connection is restored."))
            )
        }
    }
}