package com.emarsys.networking.clients.event

import com.emarsys.api.inapp.InAppConfigApi
import com.emarsys.core.channel.naturalBatching
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.networking.model.body
import com.emarsys.core.session.SessionContext
import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.mobileengage.action.EventActionFactoryApi
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

internal class EventClient(
    private val emarsysNetworkClient: NetworkClientApi,
    private val urlFactory: UrlFactoryApi,
    private val json: Json,
    private val eventActionFactory: EventActionFactoryApi,
    private val sessionContext: SessionContext,
    private val inAppConfigApi: InAppConfigApi,
    private val inAppPresenter: InAppPresenterApi,
    private val inAppViewProvider: InAppViewProviderApi,
    private val sdkEventFlow: MutableSharedFlow<SdkEvent>,
    private val onlineSdkEventFlow: Flow<SdkEvent>,
    private val sdkLogger: Logger,
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
        onlineSdkEventFlow
            .filter { it is SdkEvent.Internal.Reporting || it is SdkEvent.Internal.Sdk || it is SdkEvent.External.Custom }
            .naturalBatching().onEach { sdkEvents ->
                try {
                    sdkLogger.debug("EventClient - consumeEvents", "Batch size: ${sdkEvents.size}")
                    val url = urlFactory.create(EmarsysUrlType.EVENT, null)
                    val requestBody =
                        DeviceEventRequestBody(
                            inAppConfigApi.inAppDnd,
                            sdkEvents,
                            sessionContext.deviceEventState
                        )
                    val body = json.encodeToString(requestBody)
                    val response = emarsysNetworkClient.send(
                        UrlRequest(
                            url,
                            HttpMethod.Post,
                            body
                        )
                    ) { reEmitSdkEventsOnNetworkError(sdkEvents) }

                    if (response.status == HttpStatusCode.NoContent) {
                        return@onEach
                    }

                    val result: DeviceEventResponse = response.body()
                    handleDeviceEventState(result)
                    handleInApp(result.message)
                    handleOnAppEventAction(result)
                } catch (throwable: Throwable) {
                    sdkLogger.error("EventClient: Error during event consumption", throwable)
                }
            }.collect()
    }

    private suspend fun reEmitSdkEventsOnNetworkError(it: List<SdkEvent>) {
        it.forEach { sdkEvent -> registerEvent(sdkEvent) }
    }

    private suspend fun handleOnAppEventAction(deviceEventResponse: DeviceEventResponse) {
        deviceEventResponse.onEventAction?.let {
            sdkLogger.debug("EventClient - handleOnAppEventAction", deviceEventResponse.toString())

            val actions = it.actions
            val campaignId = it.campaignId

            actions.forEach { action ->
                eventActionFactory.create(action).invoke()
            }
            reportOnEventAction(campaignId)
        }
    }

    private suspend fun reportOnEventAction(campaignId: String) {
        sdkLogger.debug("EventClient - reportOnEventAction")

        registerEvent(SdkEvent.Internal.InApp.Viewed(attributes = buildJsonObject {
            put(
                "campaignId",
                JsonPrimitive(campaignId)
            )
        }))
    }

    private suspend fun handleDeviceEventState(deviceEventResponse: DeviceEventResponse) {
        sdkLogger.debug("EventClient - handleDeviceEventState", deviceEventResponse.toString())

        sessionContext.deviceEventState = deviceEventResponse.deviceEventState
    }

    private suspend fun handleInApp(message: EventResponseInApp?) {
        if (message != null && message.html.isNotEmpty()) {
            sdkLogger.debug(
                "EventClient - handleInApp",
                buildJsonObject { put("campaignId", JsonPrimitive(message.campaignId)) })

            val view = inAppViewProvider.provide()
            val webViewHolder = view.load(InAppMessage(message.campaignId, message.html))
            inAppPresenter.present(view, webViewHolder, InAppPresentationMode.Overlay)
        }
    }
}

