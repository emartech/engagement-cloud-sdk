package com.emarsys.networking.clients.contact

import com.emarsys.context.SdkContextApi
import com.emarsys.core.channel.SdkEventManagerApi
import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.exceptions.FailedRequestException
import com.emarsys.core.exceptions.MissingApplicationCodeException
import com.emarsys.core.exceptions.RetryLimitReachedException
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.networking.EmarsysHeaders
import com.emarsys.networking.clients.EventBasedClientApi
import com.emarsys.networking.clients.event.model.OnlineSdkEvent
import com.emarsys.networking.clients.event.model.SdkEvent
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

internal class ContactClient(
    private val emarsysClient: NetworkClientApi,
    private val sdkEventManager: SdkEventManagerApi,
    private val urlFactory: UrlFactoryApi,
    private val sdkContext: SdkContextApi,
    private val contactTokenHandler: ContactTokenHandlerApi,
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
            .collect {
                try {
                    sdkLogger.debug("ContactClient - consumeContactChanges")
                    val request = createUrlRequest(it)
                    val response = emarsysClient.send(
                        request,
                        onNetworkError = { sdkEventManager.emitEvent(it) })

                    if (response.status != HttpStatusCode.NoContent) {
                        contactTokenHandler.handleContactTokens(response)
                    }

                    setContactFields(it)

                    it.ack(eventsDao, sdkLogger)
                } catch (exception: Exception) {
                    when (exception) {
                        is FailedRequestException, is RetryLimitReachedException, is MissingApplicationCodeException -> it.ack(
                            eventsDao,
                            sdkLogger
                        )

                        else -> sdkLogger.error("ContactClient - consumeContactChanges", exception)
                    }
                }
            }
    }

    private fun setContactFields(event: SdkEvent) {
        when (event) {
            is SdkEvent.Internal.Sdk.LinkContact -> {
                sdkContext.contactFieldId = event.contactFieldId
                sdkContext.contactFieldValue = event.contactFieldValue
            }
            is SdkEvent.Internal.Sdk.LinkAuthenticatedContact -> {
                sdkContext.contactFieldId = event.contactFieldId
                sdkContext.openIdToken = event.openIdToken
            }
            is SdkEvent.Internal.Sdk.UnlinkContact -> {
                sdkContext.contactFieldId = null
                sdkContext.contactFieldValue = null
                sdkContext.openIdToken = null
            }
            else -> {}
        }
    }

    private fun isContactEvent(event: OnlineSdkEvent): Boolean {
        return event is SdkEvent.Internal.Sdk.LinkContact || event is SdkEvent.Internal.Sdk.LinkAuthenticatedContact || event is SdkEvent.Internal.Sdk.UnlinkContact
    }

    private fun createUrlRequest(event: SdkEvent): UrlRequest {
        val headers = mutableMapOf<String, Any?>()
        if (sdkContext.config?.merchantId != null) {
            headers[EmarsysHeaders.MERCHANT_ID_HEADER] = sdkContext.config!!.merchantId
        }
        return when (event) {
            is SdkEvent.Internal.Sdk.LinkContact -> {
                val requestBody = json.encodeToString(
                    LinkContactRequestBody(
                        event.contactFieldId,
                        event.contactFieldValue,
                        null
                    )
                )
                val url = urlFactory.create(EmarsysUrlType.LINK_CONTACT)
                UrlRequest(url, HttpMethod.Post, requestBody, headers)
            }

            is SdkEvent.Internal.Sdk.LinkAuthenticatedContact -> {
                val requestBody = json.encodeToString(
                    LinkContactRequestBody(
                        event.contactFieldId,
                        null,
                        event.openIdToken
                    )
                )
                val url = urlFactory.create(EmarsysUrlType.LINK_CONTACT)
                UrlRequest(url, HttpMethod.Post, requestBody, headers)
            }

            else -> {
                val url = urlFactory.create(EmarsysUrlType.UNLINK_CONTACT)
                UrlRequest(url, HttpMethod.Delete, null, headers)
            }
        }
    }
}