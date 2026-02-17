package com.sap.ec.networking.clients.contact

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
import com.sap.ec.mobileengage.session.SessionApi
import com.sap.ec.networking.clients.EventBasedClientApi
import com.sap.ec.networking.clients.error.ClientExceptionHandler
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

internal class ContactClient(
    private val ecClient: NetworkClientApi,
    private val clientExceptionHandler: ClientExceptionHandler,
    private val sdkEventManager: SdkEventManagerApi,
    private val urlFactory: UrlFactoryApi,
    private val sdkContext: SdkContextApi,
    private val contactTokenHandler: ContactTokenHandlerApi,
    private val ecSdkSession: SessionApi,
    private val eventsDao: EventsDaoApi,
    private val json: Json,
    private val sdkLogger: Logger,
    private val sdkDispatcher: CoroutineDispatcher
) : EventBasedClientApi {

    override suspend fun register() {
        CoroutineScope(sdkDispatcher).launch(start = CoroutineStart.UNDISPATCHED) {
            sdkLogger.debug("ContactClient - register")
            startEventConsumer()
        }
    }

    private suspend fun startEventConsumer() {
        sdkEventManager.onlineSdkEvents
            .filter { isContactEvent(it) }
            .collect { sdkEvent ->
                try {
                    sdkLogger.debug("ContactClient - consumeContactChanges")
                    val request = createUrlRequest(sdkEvent)
                    val response = ecClient.send(
                        request
                    )
                    response.onSuccess {
                        if (it.status != HttpStatusCode.NoContent) {
                            contactTokenHandler.handleContactTokens(it)
                        }
                        handleSuccess(sdkEvent)
                    }
                    response.onFailure { throwable ->
                        handleException(throwable, sdkEvent)
                    }
                } catch (throwable: Throwable) {
                    handleException(throwable, sdkEvent)
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
            "ContactClient - consumeContactChanges",
            sdkEvent
        )
    }

    private suspend fun handleSuccess(event: OnlineSdkEvent) {
        when (event) {
            is SdkEvent.Internal.Sdk.LinkContact -> {
                sdkContext.contactFieldValue = event.contactFieldValue
                ecSdkSession.startSession()
            }

            is SdkEvent.Internal.Sdk.LinkAuthenticatedContact -> {
                sdkContext.openIdToken = event.openIdToken
                ecSdkSession.startSession()
            }

            is SdkEvent.Internal.Sdk.UnlinkContact -> {
                sdkContext.contactFieldValue = null
                sdkContext.openIdToken = null
                ecSdkSession.endSession()
            }

            else -> {}
        }

        event.ack(eventsDao, sdkLogger)
    }

    private fun isContactEvent(event: OnlineSdkEvent): Boolean {
        return event is SdkEvent.Internal.Sdk.LinkContact || event is SdkEvent.Internal.Sdk.LinkAuthenticatedContact || event is SdkEvent.Internal.Sdk.UnlinkContact
    }

    private fun createUrlRequest(event: SdkEvent): UrlRequest {
        val headers = mutableMapOf<String, Any?>()
        return when (event) {
            is SdkEvent.Internal.Sdk.LinkContact -> {
                val requestBody = json.encodeToString(
                    LinkContactRequestBody(
                        event.contactFieldValue,
                        null
                    )
                )
                val url = urlFactory.create(ECUrlType.LinkContact)
                UrlRequest(url, HttpMethod.Post, requestBody, headers)
            }

            is SdkEvent.Internal.Sdk.LinkAuthenticatedContact -> {
                val requestBody = json.encodeToString(
                    LinkContactRequestBody(
                        null,
                        event.openIdToken
                    )
                )
                val url = urlFactory.create(ECUrlType.LinkContact)
                UrlRequest(url, HttpMethod.Post, requestBody, headers)
            }

            else -> {
                val url = urlFactory.create(ECUrlType.UnlinkContact)
                UrlRequest(url, HttpMethod.Delete, null, headers)
            }
        }
    }
}