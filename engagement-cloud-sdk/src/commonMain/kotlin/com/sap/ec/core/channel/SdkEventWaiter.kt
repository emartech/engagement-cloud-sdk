package com.sap.ec.core.channel

import com.sap.ec.event.SdkEvent
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration

internal class SdkEventWaiter(
    override val sdkEventDistributor: SdkEventDistributorApi,
    override val sdkEvent: SdkEvent,
    override val connectionStatus: StateFlow<Boolean>
) : SdkEventWaiterApi {

    override suspend fun <T> await(): SdkEvent.Internal.Sdk.Answer.Response<T> {
        return awaitResponse()
    }

    override suspend fun <T> await(timeout: Duration): SdkEvent.Internal.Sdk.Answer.Response<T> {
        return try {
            withTimeout(timeout) {
                awaitResponse()
            }
        } catch (exception: TimeoutCancellationException) {
            SdkEvent.Internal.Sdk.Answer.Response(
                sdkEvent.id,
                Result.failure(exception)
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun <T> awaitResponse(): SdkEvent.Internal.Sdk.Answer.Response<T> {
        return if (sdkEvent is SdkEvent.Internal.Sdk.Answer.Response<*>) {
            sdkEvent as SdkEvent.Internal.Sdk.Answer.Response<T>
        } else if (connectionStatus.value) {
            sdkEventDistributor.sdkEventFlow.first {
                it is SdkEvent.Internal.Sdk.Answer && sdkEvent.id == it.originId
            } as SdkEvent.Internal.Sdk.Answer.Response<T>
        } else {
            SdkEvent.Internal.Sdk.Answer.Response(
                sdkEvent.id,
                Result.failure(Exception("No internet connection. Event: ${sdkEvent.type} is stored and will be processed when connection is restored."))
            )
        }
    }
}