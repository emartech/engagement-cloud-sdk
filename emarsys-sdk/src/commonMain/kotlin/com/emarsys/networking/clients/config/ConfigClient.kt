package com.emarsys.networking.clients.config

import com.emarsys.context.SdkContextApi
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.session.SessionContext
import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.di.DispatcherTypes
import com.emarsys.di.EventFlowTypes
import com.emarsys.di.NetworkClientTypes
import com.emarsys.di.SdkComponent
import com.emarsys.networking.RefreshTokenRequestBody
import com.emarsys.networking.clients.contact.ContactTokenHandlerApi
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

internal object ConfigClient : ConfigClientApi, SdkComponent {
    private val emarsysNetworkClient: NetworkClientApi by inject(named(NetworkClientTypes.Emarsys))
    private val urlFactory: UrlFactoryApi by inject()
    private val sdkEventFlow: MutableSharedFlow<SdkEvent> by inject(named(EventFlowTypes.InternalEventFlow))
    private val sessionContext: SessionContext by inject()
    private val sdkContext: SdkContextApi by inject()
    private val contactTokenHandler: ContactTokenHandlerApi by inject()
    private val json: Json by inject()
    private val sdkLogger: Logger by inject { parametersOf(ConfigClient::class.simpleName) }
    private val sdkDispatcher: CoroutineDispatcher by inject(named(DispatcherTypes.Sdk))

    init {
        CoroutineScope(sdkDispatcher).launch {
            startEventConsumer()
        }
    }

    private suspend fun startEventConsumer() {
        sdkEventFlow
            .filter { it is SdkEvent.Internal.Sdk.ChangeAppCode || it is SdkEvent.Internal.Sdk.ChangeMerchantId }
            .collect {
                sdkLogger.debug("ConfigClient - consumeConfigChanges")
                val request = if (it is SdkEvent.Internal.Sdk.ChangeAppCode) {
                    val url = urlFactory.create(EmarsysUrlType.CHANGE_APPLICATION_CODE, null)
                    UrlRequest(url, HttpMethod.Post)
                } else {
                    val url = urlFactory.create(EmarsysUrlType.REFRESH_TOKEN)

                    UrlRequest(
                        url, HttpMethod.Post,
                        json.encodeToString(
                            RefreshTokenRequestBody(sessionContext.refreshToken!!)
                        ),
                    )
                }

                val response = emarsysNetworkClient.send(request)
                if (response.status == HttpStatusCode.OK) {
                    contactTokenHandler.handleContactTokens(response)
                    if (it is SdkEvent.Internal.Sdk.ChangeMerchantId) {
                        sdkContext.config =
                            sdkContext.config?.copyWith(merchantId = it.attributes?.get("merchantId")?.jsonPrimitive?.contentOrNull)
                    } else if (it is SdkEvent.Internal.Sdk.ChangeAppCode) {
                        sdkContext.config =
                            sdkContext.config?.copyWith(applicationCode = it.attributes?.get("applicationCode")?.jsonPrimitive?.contentOrNull)
                    }
                    return@collect
                }

            }
    }
}