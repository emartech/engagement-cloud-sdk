package com.emarsys.networking.clients.event

import com.emarsys.api.inapp.InAppConfig
import com.emarsys.core.channel.naturalBatching
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.networking.model.body
import com.emarsys.core.session.SessionContext
import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.inapp.InAppMessage
import com.emarsys.mobileengage.inapp.InAppPresentationMode
import com.emarsys.mobileengage.inapp.InAppPresenterApi
import com.emarsys.mobileengage.inapp.InAppViewProviderApi
import com.emarsys.networking.clients.event.model.DeviceEventRequestBody
import com.emarsys.networking.clients.event.model.DeviceEventResponse
import com.emarsys.networking.clients.event.model.EventResponseInApp
import com.emarsys.networking.clients.event.model.SdkEvent
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

class EventClient(
    private val emarsysNetworkClient: NetworkClientApi,
    private val urlFactory: UrlFactoryApi,
    private val json: Json,
    private val onEventActionFactory: ActionFactoryApi<ActionModel>,
    private val sessionContext: SessionContext,
    private val inAppConfig: InAppConfig,
    private val inAppPresenter: InAppPresenterApi,
    private val inAppViewProvider: InAppViewProviderApi,
    private val sdkEventFlow: MutableSharedFlow<SdkEvent>,
    private val logger: Logger,
    sdkDispatcher: CoroutineDispatcher
) : EventClientApi {

    init {
        CoroutineScope(sdkDispatcher).launch {
            startEventConsumer()
        }
    }

    override suspend fun registerEvent(event: SdkEvent) {
        sdkEventFlow.emit(event)
    }

    private suspend fun startEventConsumer() {
        sdkEventFlow
            .filter { it is SdkEvent.Internal.Reporting || it is SdkEvent.Internal.Sdk || it is SdkEvent.External.Custom }
            .naturalBatching().onEach {
                try {
                    val url = urlFactory.create(EmarsysUrlType.EVENT)
                    val requestBody =
                        DeviceEventRequestBody(
                            inAppConfig.inAppDnd,
                            it,
                            sessionContext.deviceEventState
                        )
                    val body = json.encodeToString(requestBody)
                    val response = emarsysNetworkClient.send(UrlRequest(url, HttpMethod.Post, body))

                    if (response.status == HttpStatusCode.NoContent) {
                        return@onEach
                    }

                    val result: DeviceEventResponse = response.body()
                    handleDeviceEventState(result)
                    handleInApp(result.message)
                    handleOnAppEventAction(result)
                } catch (e: Exception) {
                    logger.error("EventClient: Error during event consumption", e)
                }
            }.collect()
    }

    private suspend fun handleOnAppEventAction(deviceEventResponse: DeviceEventResponse) {
        deviceEventResponse.onEventAction?.let {
            val actions = it.actions
            val campaignId = it.campaignId

            actions.forEach { action ->
                onEventActionFactory.create(action).invoke()
            }
            reportOnEventAction(campaignId)
        }
    }

    private suspend fun reportOnEventAction(campaignId: String) {
        registerEvent(SdkEvent.Internal.InApp.Viewed(attributes = buildJsonObject {
            put(
                "campaignId",
                JsonPrimitive(campaignId)
            )
        }))
    }

    private fun handleDeviceEventState(deviceEventResponse: DeviceEventResponse) {
        sessionContext.deviceEventState = deviceEventResponse.deviceEventState
    }

    private suspend fun handleInApp(message: EventResponseInApp?) {
        if (message != null && message.html.isNotEmpty()) {
            val view = inAppViewProvider.provide()
            view.load(InAppMessage(html = message.html))
            inAppPresenter.present(view, InAppPresentationMode.Overlay)
        }
    }
}

