package com.emarsys.networking.clients.contact

import com.emarsys.context.SdkContextApi
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.networking.EmarsysHeaders
import com.emarsys.networking.clients.event.model.SdkEvent
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

internal class ContactClient(
    private val emarsysClient: NetworkClientApi,
    private val sdkEventDistributor: SdkEventDistributorApi,
    private val urlFactory: UrlFactoryApi,
    private val sdkContext: SdkContextApi,
    private val contactTokenHandler: ContactTokenHandlerApi,
    private val json: Json,
    private val sdkLogger: Logger,
    sdkDispatcher: CoroutineDispatcher
) {

    init {
        CoroutineScope(sdkDispatcher).launch {
            startEventConsumer()
        }
    }

    private suspend fun startEventConsumer() {
        sdkEventDistributor.onlineEvents
            .filter { isContactEvent(it) }
            .collect {
                try {
                    sdkLogger.debug("ContactClient - consumeContactChanges")
                    val request = createUrlRequest(it)
                    val response = emarsysClient.send(request)

                    if (response.status.isSuccess()) {
                        if(response.status != HttpStatusCode.NoContent) {
                            contactTokenHandler.handleContactTokens(response)
                        }
                        sdkContext.contactFieldId =
                            it.attributes?.get("contactFieldId")?.jsonPrimitive?.content?.toInt()
                    }
                    return@collect
                } catch (exception: Exception) {
                    sdkLogger.error("ContactClient - consumeContactChanges", exception)
                }
            }
    }

    private fun isContactEvent(event: SdkEvent): Boolean {
        return event is SdkEvent.Internal.Sdk.LinkContact || event is SdkEvent.Internal.Sdk.LinkAuthenticatedContact || event is SdkEvent.Internal.Sdk.UnlinkContact
    }

    private fun createUrlRequest(event: SdkEvent): UrlRequest {
        val headers = mutableMapOf<String, Any?>()
        if (sdkContext.config?.merchantId != null) {
            headers[EmarsysHeaders.MERCHANT_ID_HEADER] = sdkContext.config!!.merchantId
        }
        return if (event is SdkEvent.Internal.Sdk.LinkContact || event is SdkEvent.Internal.Sdk.LinkAuthenticatedContact) {
            val requestBody = json.encodeToString(
                LinkContactRequestBody(
                    event.attributes?.get("contactFieldId")?.jsonPrimitive?.content!!.toInt(),
                    event.attributes?.get("contactFieldValue")?.jsonPrimitive?.contentOrNull,
                    event.attributes?.get("openIdToken")?.jsonPrimitive?.contentOrNull,
                )
            )
            val url = urlFactory.create(EmarsysUrlType.LINK_CONTACT, null)
            UrlRequest(url, HttpMethod.Post, requestBody, headers)
        } else {
            val url = urlFactory.create(EmarsysUrlType.UNLINK_CONTACT, null)
            UrlRequest(url, HttpMethod.Delete, null, headers)
        }
    }
}