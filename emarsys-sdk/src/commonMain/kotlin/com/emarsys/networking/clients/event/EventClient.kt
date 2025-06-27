package com.emarsys.networking.clients.event

import com.emarsys.api.inapp.InAppConfigApi
import com.emarsys.core.channel.SdkEventManagerApi
import com.emarsys.core.channel.naturalBatching
import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.exceptions.FailedRequestException
import com.emarsys.core.exceptions.MissingApplicationCodeException
import com.emarsys.core.exceptions.RetryLimitReachedException
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.context.RequestContextApi
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.networking.model.body
import com.emarsys.core.providers.UuidProviderApi
import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.event.SdkEvent
import com.emarsys.event.ack
import com.emarsys.mobileengage.action.EventActionFactoryApi
import com.emarsys.mobileengage.inapp.InAppMessage
import com.emarsys.mobileengage.inapp.InAppPresentationMode
import com.emarsys.mobileengage.inapp.InAppPresenterApi
import com.emarsys.mobileengage.inapp.InAppType
import com.emarsys.mobileengage.inapp.InAppViewProviderApi
import com.emarsys.networking.clients.EventBasedClientApi
import com.emarsys.networking.clients.event.model.DeviceEventRequestBody
import com.emarsys.networking.clients.event.model.DeviceEventResponse
import com.emarsys.networking.clients.event.model.EventResponseInApp
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
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
    private val requestContext: RequestContextApi,
    private val inAppConfigApi: InAppConfigApi,
    private val inAppPresenter: InAppPresenterApi,
    private val inAppViewProvider: InAppViewProviderApi,
    private val sdkEventManager: SdkEventManagerApi,
    private val eventsDao: EventsDaoApi,
    private val sdkLogger: Logger,
    private val applicationScope: CoroutineScope,
    private val uuidProvider: UuidProviderApi
) : EventBasedClientApi {

    override suspend fun register() {
        applicationScope.launch(start = CoroutineStart.UNDISPATCHED) {
            startEventConsumer()
        }
    }

    private suspend fun startEventConsumer() {
        sdkEventManager.onlineSdkEvents
            .filter { it is SdkEvent.Internal.Reporting || it is SdkEvent.Internal.Custom || it is SdkEvent.External.Custom }
            .naturalBatching().onEach { sdkEvents ->
                try {
                    sdkLogger.debug("Consume Events, Batch size: ${sdkEvents.size}")
                    val url = urlFactory.create(EmarsysUrlType.EVENT)
                    val requestBody =
                        DeviceEventRequestBody(
                            inAppConfigApi.inAppDnd,
                            sdkEvents,
                            requestContext.deviceEventState
                        )
                    val body = json.encodeToString(requestBody)
                    val response = emarsysNetworkClient.send(
                        UrlRequest(
                            url,
                            HttpMethod.Post,
                            body
                        ),
                        onNetworkError = { reEmitSdkEventsOnNetworkError(sdkEvents) }
                    )

                    if (response.status == HttpStatusCode.NoContent) {
                        sdkEvents.ack(eventsDao, sdkLogger)
                        return@onEach
                    }

                    val result: DeviceEventResponse = response.body()
                    handleDeviceEventState(result)
                    handleInApp(result.message)
                    handleOnAppEventAction(result)
                    sdkEvents.forEach {
                        sdkEventManager.emitEvent(SdkEvent.Internal.Sdk.Answer.Ready(originId = it.id))
                    }
                    sdkEvents.ack(eventsDao, sdkLogger)
                } catch (throwable: Throwable) {
                    when (throwable) {
                        is FailedRequestException, is RetryLimitReachedException, is MissingApplicationCodeException -> {
                            sdkEvents.ack(eventsDao, sdkLogger)
                        }

                        else -> sdkLogger.error(
                            "EventClient: Error during event consumption",
                            throwable
                        )
                    }
                }
            }.collect()
    }

    private suspend fun reEmitSdkEventsOnNetworkError(it: List<SdkEvent>) {
        it.forEach { sdkEvent -> sdkEventManager.emitEvent(sdkEvent) }
    }

    private suspend fun handleOnAppEventAction(deviceEventResponse: DeviceEventResponse) {
        deviceEventResponse.onEventAction?.let {
            sdkLogger.debug(deviceEventResponse.toString())

            val actions = it.actions
            val campaignId = it.campaignId

            actions.forEach { action ->
                eventActionFactory.create(action).invoke()
            }
            reportOnEventAction(campaignId)
        }
    }

    //TODO: check what needs to be reported here
    private suspend fun reportOnEventAction(campaignId: String) {
        sdkLogger.debug("EventClient - reportOnEventAction")

        sdkEventManager.registerEvent(SdkEvent.Internal.InApp.Viewed(attributes = buildJsonObject {
            put(
                "campaignId",
                JsonPrimitive(campaignId)
            )
        }))
    }

    private suspend fun handleDeviceEventState(deviceEventResponse: DeviceEventResponse) {
        sdkLogger.debug(deviceEventResponse.toString())

        requestContext.deviceEventState = deviceEventResponse.deviceEventState
    }

    private suspend fun handleInApp(message: EventResponseInApp?) {
        if (message != null && message.html.isNotEmpty()) {
            sdkLogger.debug(
                "EventClient - handleInApp",
                buildJsonObject { put("campaignId", JsonPrimitive(message.campaignId)) })

            val view = inAppViewProvider.provide()
            //TODO: will be modified after event service v5 migration
            val webViewHolder =
                view.load(
                    InAppMessage(
                        dismissId = uuidProvider.provide(),
                        type = InAppType.OVERLAY,
                        trackingInfo = message.campaignId,
                        content = message.html
                    )
                )
            inAppPresenter.present(view, webViewHolder, InAppPresentationMode.Overlay)
        }
    }
}

