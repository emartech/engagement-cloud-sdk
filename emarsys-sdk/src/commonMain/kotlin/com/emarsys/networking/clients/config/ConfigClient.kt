package com.emarsys.networking.clients.config

import com.emarsys.context.SdkContextApi
import com.emarsys.core.channel.SdkEventManagerApi
import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.context.RequestContextApi
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.event.SdkEvent
import com.emarsys.networking.RefreshTokenRequestBody
import com.emarsys.networking.clients.EventBasedClientApi
import com.emarsys.networking.clients.contact.ContactTokenHandlerApi
import com.emarsys.networking.clients.error.ClientExceptionHandler
import io.ktor.http.HttpMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

internal class ConfigClient(
    private val emarsysNetworkClient: NetworkClientApi,
    private val clientExceptionHandler: ClientExceptionHandler,
    private val urlFactory: UrlFactoryApi,
    private val sdkEventManager: SdkEventManagerApi,
    private val requestContext: RequestContextApi,
    private val sdkContext: SdkContextApi,
    private val contactTokenHandler: ContactTokenHandlerApi,
    private val eventsDao: EventsDaoApi,
    private val json: Json,
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
            .filter { it is SdkEvent.Internal.Sdk.ChangeAppCode || it is SdkEvent.Internal.Sdk.ChangeMerchantId }
            .collect {
                try {
                    sdkLogger.debug("ConfigClient - consumeConfigChanges")
                    val request = if (it is SdkEvent.Internal.Sdk.ChangeAppCode) {
                        val url = urlFactory.create(EmarsysUrlType.CHANGE_APPLICATION_CODE)
                        UrlRequest(url, HttpMethod.Post)
                    } else {
                        val url = urlFactory.create(EmarsysUrlType.REFRESH_TOKEN)

                        UrlRequest(
                            url, HttpMethod.Post,
                            json.encodeToString(
                                RefreshTokenRequestBody(requestContext.refreshToken!!)
                            ),
                        )
                    }

                    val response =
                        emarsysNetworkClient.send(
                            request,
                            onNetworkError = { sdkEventManager.emitEvent(it) }
                        )
                    contactTokenHandler.handleContactTokens(response)
                    if (it is SdkEvent.Internal.Sdk.ChangeMerchantId) {
                        sdkContext.config =
                            sdkContext.config?.copyWith(merchantId = it.merchantId)
                    } else if (it is SdkEvent.Internal.Sdk.ChangeAppCode) {
                        sdkContext.config =
                            sdkContext.config?.copyWith(applicationCode = it.applicationCode)
                    }
                    it.ack(eventsDao, sdkLogger)
                } catch (exception: Exception) {
                    clientExceptionHandler.handleException(
                        exception,
                        "ConfigClient - consumeConfigChanges",
                        it
                    )
                }
            }
    }
}