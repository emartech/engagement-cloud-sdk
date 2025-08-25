package com.emarsys.networking.clients.push

import com.emarsys.core.channel.SdkEventManagerApi
import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.url.EmarsysUrlType.PUSH_TOKEN
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.event.OnlineSdkEvent
import com.emarsys.event.SdkEvent
import com.emarsys.networking.clients.EventBasedClientApi
import com.emarsys.networking.clients.error.ClientExceptionHandler
import com.emarsys.networking.clients.push.model.PushToken
import io.ktor.http.HttpMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class PushClient(
    private val emarsysClient: NetworkClientApi,
    private val clientExceptionHandler: ClientExceptionHandler,
    private val urlFactory: UrlFactoryApi,
    private val sdkEventManager: SdkEventManagerApi,
    private val applicationScope: CoroutineScope,
    private val eventsDao: EventsDaoApi,
    private val json: Json,
    private val sdkLogger: Logger
) : EventBasedClientApi {

    override suspend fun register() {
        applicationScope.launch(start = CoroutineStart.UNDISPATCHED) {
            sdkLogger.debug("Register")
            startEventConsumer()
        }
    }

    private suspend fun startEventConsumer() {
        sdkEventManager.onlineSdkEvents
            .filter { it is SdkEvent.Internal.Sdk.RegisterPushToken || it is SdkEvent.Internal.Sdk.ClearPushToken }
            .collect {
                try {
                    sdkLogger.debug("PushClient - consumePushEvents")
                    createRequest(it)?.let { request ->
                        emarsysClient.send(
                            request
                        )
                    }
                    sdkEventManager.emitEvent(SdkEvent.Internal.Sdk.Answer.Ready(originId = it.id))
                    it.ack(eventsDao, sdkLogger)
                } catch (exception: Exception) {
                    clientExceptionHandler.handleException(
                        exception,
                        "PushClient - consumePushEvents",
                        it
                    )
                }
            }
    }

    private fun createRequest(sdkEvent: OnlineSdkEvent): UrlRequest? {
        val url = urlFactory.create(PUSH_TOKEN)
        return when (sdkEvent) {
            is SdkEvent.Internal.Sdk.RegisterPushToken -> {
                val pushToken = sdkEvent.pushToken
                val body = json.encodeToString(PushToken(pushToken))
                UrlRequest(url, HttpMethod.Put, body)
            }

            is SdkEvent.Internal.Sdk.ClearPushToken -> UrlRequest(url, HttpMethod.Delete)
            else -> null
        }
    }
}