package com.emarsys.networking.clients.config

import com.emarsys.context.SdkContextApi
import com.emarsys.core.channel.SdkEventManagerApi
import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.exceptions.SdkException.NetworkIOException
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.event.OnlineSdkEvent
import com.emarsys.event.SdkEvent
import com.emarsys.networking.clients.EventBasedClientApi
import com.emarsys.networking.clients.contact.ContactTokenHandlerApi
import com.emarsys.networking.clients.error.ClientExceptionHandler
import io.ktor.http.HttpMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

internal class ConfigClient(
    private val emarsysNetworkClient: NetworkClientApi,
    private val clientExceptionHandler: ClientExceptionHandler,
    private val urlFactory: UrlFactoryApi,
    private val sdkEventManager: SdkEventManagerApi,
    private val sdkContext: SdkContextApi,
    private val contactTokenHandler: ContactTokenHandlerApi,
    private val eventsDao: EventsDaoApi,
    private val sdkLogger: Logger,
    private val applicationScope: CoroutineScope
) : EventBasedClientApi {

    override suspend fun register() {
        applicationScope.launch(start = CoroutineStart.UNDISPATCHED) {
            sdkLogger.debug("Register")
            startEventConsumer()
        }
    }

    private suspend fun startEventConsumer() {
        sdkEventManager.onlineSdkEvents
            .filter { it is SdkEvent.Internal.Sdk.ChangeAppCode }
            .collect { sdkEvent ->
                try {
                    sdkLogger.debug("ConfigClient - consumeConfigChanges")

                    val url = urlFactory.create(EmarsysUrlType.ChangeApplicationCode)
                    val request = UrlRequest(url, HttpMethod.Post)
                    val response = emarsysNetworkClient.send(request)
                    response.onSuccess {
                        contactTokenHandler.handleContactTokens(it)
                        if (sdkEvent is SdkEvent.Internal.Sdk.ChangeAppCode) {
                            sdkContext.config =
                                sdkContext.config?.copyWith(applicationCode = sdkEvent.applicationCode)
                        }
                        sdkEvent.ack(eventsDao, sdkLogger)
                    }
                    response.onFailure { throwable ->
                        handleException(throwable, sdkEvent)
                    }
                } catch (e: Exception) {
                    handleException(e, sdkEvent)
                }
            }
    }

    private suspend fun handleException(exception: Throwable, sdkEvent: OnlineSdkEvent) {
        if (exception is NetworkIOException) {
            sdkEventManager.emitEvent(sdkEvent)
        } else {
            SdkEvent.Internal.Sdk.Answer.Response(
                originId = sdkEvent.id,
                Result.failure<Exception>(exception)
            )

        }
        clientExceptionHandler.handleException(
            exception,
            "ConfigClient - consumeConfigChanges",
            sdkEvent
        )
    }
}