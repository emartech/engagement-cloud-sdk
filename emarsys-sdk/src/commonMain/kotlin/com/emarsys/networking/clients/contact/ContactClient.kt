package com.emarsys.networking.clients.contact

import com.emarsys.context.SdkContextApi
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.di.DispatcherTypes
import com.emarsys.di.EventFlowTypes
import com.emarsys.di.NetworkClientTypes
import com.emarsys.di.SdkComponent
import com.emarsys.networking.EmarsysHeaders
import com.emarsys.networking.clients.event.model.SdkEvent
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named

internal object ContactClient : ContactClientApi, SdkComponent {
    private val emarsysClient: NetworkClientApi by inject(named(NetworkClientTypes.Emarsys))
    private val sdkEventFlow: MutableSharedFlow<SdkEvent> by inject(named(EventFlowTypes.InternalEventFlow))
    private val urlFactory: UrlFactoryApi by inject()
    private val sdkContext: SdkContextApi by inject()
    private val contactTokenHandler: ContactTokenHandlerApi by inject()
    private val json: Json by inject()
    private val sdkLogger: Logger by inject { parametersOf(ContactClient::class.simpleName) }
    private val sdkDispatcher: CoroutineDispatcher by inject(named(DispatcherTypes.Sdk))

    init {
        CoroutineScope(sdkDispatcher).launch {
            startEventConsumer()
        }
    }

    private suspend fun startEventConsumer() {
        sdkEventFlow
            .filter { isContactEvent(it) }
            .collect {
                sdkLogger.debug("ContactClient - consumeContactChanges")
                val request = createUrlRequest(it)
                val response = emarsysClient.send(request)

                if (response.status == HttpStatusCode.OK) {
                    contactTokenHandler.handleContactTokens(response)
                    sdkContext.contactFieldId =
                        it.attributes?.get("contactFieldId")?.jsonPrimitive?.content?.toInt()
                }
                return@collect
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