package com.sap.ec.networking.clients.config

import com.sap.ec.api.SdkState
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.channel.SdkEventManagerApi
import com.sap.ec.core.db.events.EventsDaoApi
import com.sap.ec.core.exceptions.SdkException.NetworkIOException
import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.clients.NetworkClientApi
import com.sap.ec.core.networking.model.UrlRequest
import com.sap.ec.core.url.ECUrlType
import com.sap.ec.core.url.UrlFactoryApi
import com.sap.ec.event.OnlineSdkEvent
import com.sap.ec.event.SdkEvent
import com.sap.ec.mobileengage.config.FollowUpChangeAppCodeOrganizerApi
import com.sap.ec.networking.clients.EventBasedClientApi
import com.sap.ec.networking.clients.contact.ContactTokenHandlerApi
import com.sap.ec.networking.clients.error.ClientExceptionHandler
import io.ktor.http.HttpMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

internal class ConfigClient(
    private val ecNetworkClient: NetworkClientApi,
    private val clientExceptionHandler: ClientExceptionHandler,
    private val urlFactory: UrlFactoryApi,
    private val sdkEventManager: SdkEventManagerApi,
    private val sdkContext: SdkContextApi,
    private val contactTokenHandler: ContactTokenHandlerApi,
    private val followUpChangeAppCodeOrganizer: FollowUpChangeAppCodeOrganizerApi,
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

                    val url = urlFactory.create(ECUrlType.ChangeApplicationCode)
                    val request = UrlRequest(url, HttpMethod.Post)
                    val response = ecNetworkClient.send(request)
                    response.onSuccess {
                        sdkContext.setSdkState(SdkState.OnHold)
                        contactTokenHandler.handleContactTokens(it)
                        if (sdkEvent is SdkEvent.Internal.Sdk.ChangeAppCode) {
                            sdkContext.config =
                                sdkContext.config?.copyWith(applicationCode = sdkEvent.applicationCode)
                        }

                        followUpChangeAppCodeOrganizer.organize()

                        sdkContext.setSdkState(SdkState.Active)
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