package com.emarsys.networking.clients.event

import com.emarsys.api.inapp.InAppConfigApi
import com.emarsys.core.channel.SdkEventManagerApi
import com.emarsys.core.channel.naturalBatching
import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.exceptions.SdkException.NetworkIOException
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.context.RequestContextApi
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.networking.model.body
import com.emarsys.core.providers.UuidProviderApi
import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.event.OnlineSdkEvent
import com.emarsys.event.SdkEvent
import com.emarsys.event.ack
import com.emarsys.mobileengage.action.EventActionFactoryApi
import com.emarsys.mobileengage.action.models.BasicActionModel
import com.emarsys.mobileengage.inapp.InAppMessage
import com.emarsys.mobileengage.inapp.InAppType
import com.emarsys.networking.clients.EventBasedClientApi
import com.emarsys.networking.clients.error.ClientExceptionHandler
import com.emarsys.networking.clients.event.model.ContentCampaign
import com.emarsys.networking.clients.event.model.DeviceEventRequestBody
import com.emarsys.networking.clients.event.model.DeviceEventResponse
import com.emarsys.networking.clients.event.model.OnEventActionCampaign
import com.emarsys.networking.clients.event.model.toDeviceEvent
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.coroutines.coroutineContext
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class EventClient(
    private val emarsysNetworkClient: NetworkClientApi,
    private val clientExceptionHandler: ClientExceptionHandler,
    private val urlFactory: UrlFactoryApi,
    private val json: Json,
    private val eventActionFactory: EventActionFactoryApi,
    private val requestContext: RequestContextApi,
    private val inAppConfigApi: InAppConfigApi,
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
            .filter { it is SdkEvent.DeviceEvent }
            .naturalBatching().onEach { sdkEvents ->
                try {
                    sdkLogger.debug("Consume Events, Batch size: ${sdkEvents.size}")
                    val url = urlFactory.create(EmarsysUrlType.EVENT)
                    val requestBody =
                        DeviceEventRequestBody(
                            inAppConfigApi.inAppDnd,
                            sdkEvents.map { (it as SdkEvent.DeviceEvent).toDeviceEvent() },
                            requestContext.deviceEventState
                        )
                    val body = json.encodeToString(requestBody)
                    val networkResponse = emarsysNetworkClient.send(
                        UrlRequest(
                            url,
                            HttpMethod.Post,
                            body
                        )
                    )
                    networkResponse.onSuccess { response ->
                        if (response.status == HttpStatusCode.NoContent) {
                            sdkEvents.ack(eventsDao, sdkLogger)
                            return@onEach
                        }
                        val result: DeviceEventResponse = response.body()
                        handleDeviceEventState(result)
                        handleInApp(result.contentCampaigns?.getOrNull(0))  // todo handle all campaigns returned
                        result.actionCampaigns?.let { handleOnEventActionCampaigns(it) }
                        sdkEvents.forEach {
                            sdkEventManager.emitEvent(
                                SdkEvent.Internal.Sdk.Answer.Response(
                                    originId = it.id,
                                    Result.success(response)
                                )
                            )
                        }
                        sdkEvents.ack(eventsDao, sdkLogger)
                    }
                    networkResponse.onFailure { exception ->
                        if (exception is NetworkIOException) {
                            reEmitSdkEventsOnNetworkError(sdkEvents)
                        } else {
                            handleException(exception, sdkEvents)
                        }
                    }
                } catch (e: Exception) {
                    handleException(e, sdkEvents)
                }
            }.collect()
    }

    private suspend fun handleException(
        exception: Throwable,
        sdkEvents: List<OnlineSdkEvent>
    ) {
        clientExceptionHandler.handleException(
            exception,
            "EventClient: Error during event consumption",
            *sdkEvents.toTypedArray()
        )
        sdkEvents.forEach {
            sdkEventManager.emitEvent(
                SdkEvent.Internal.Sdk.Answer.Response(
                    originId = it.id,
                    Result.failure<Throwable>(exception)
                )
            )
        }
    }

    private suspend fun reEmitSdkEventsOnNetworkError(it: List<SdkEvent>) {
        it.forEach { sdkEvent -> sdkEventManager.emitEvent(sdkEvent) }
    }

    private suspend fun handleOnEventActionCampaigns(onEventActionCampaigns: List<OnEventActionCampaign>) {
        onEventActionCampaigns.forEach { campaign ->
            val actions =
                campaign.actions.joinToString(separator = ", ") { it::class.simpleName.toString() }
            sdkLogger.debug("EventClient - handleOnEventActionCampaigns with: $actions")

            campaign.actions.forEach { action ->
                try {
                    eventActionFactory.create(action).invoke()
                    reportOnEventActionCampaignAction(campaign.trackingInfo, action)
                } catch (exception: Exception) {
                    coroutineContext.ensureActive()
                    sdkLogger.error("EventClient - onEventActionInvocationFailed", exception)
                }
            }
        }
    }

    private suspend fun reportOnEventActionCampaignAction(
        trackingInfo: String,
        action: BasicActionModel
    ) {
        sdkLogger.debug("EventClient - reportOnEventActionCampaignAction")
        sdkEventManager.registerEvent(
            SdkEvent.Internal.OnEventActionExecuted(
                trackingInfo = trackingInfo,
                reporting = action.reporting,
            )
        )
    }

    private suspend fun handleDeviceEventState(deviceEventResponse: DeviceEventResponse) {
        sdkLogger.debug(deviceEventResponse.toString())

        requestContext.deviceEventState = deviceEventResponse.deviceEventState
    }

    private suspend fun handleInApp(message: ContentCampaign?) {
        if (message != null && message.content.isNotEmpty()) {
            sdkLogger.debug(
                "EventClient - handleInApp",
                buildJsonObject { put("campaignId", JsonPrimitive(message.trackingInfo)) })

            sdkEventManager.emitEvent(SdkEvent.Internal.InApp.Present(
                inAppMessage = InAppMessage(
                    dismissId = uuidProvider.provide(),
                    type = InAppType.OVERLAY,
                    trackingInfo = message.trackingInfo,
                    content = message.content
                )
            ))
        }
    }
}

