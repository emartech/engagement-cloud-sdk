package com.sap.ec.networking.clients.event

import com.sap.ec.api.inapp.InAppConfigApi
import com.sap.ec.core.channel.SdkEventManagerApi
import com.sap.ec.core.channel.naturalBatching
import com.sap.ec.core.db.events.EventsDaoApi
import com.sap.ec.core.device.DeviceInfoCollectorApi
import com.sap.ec.core.exceptions.SdkException.NetworkIOException
import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.clients.NetworkClientApi
import com.sap.ec.core.networking.context.RequestContextApi
import com.sap.ec.core.networking.model.Response
import com.sap.ec.core.networking.model.UrlRequest
import com.sap.ec.core.networking.model.body
import com.sap.ec.core.providers.UuidProviderApi
import com.sap.ec.core.providers.pagelocation.PageLocationProviderApi
import com.sap.ec.core.url.ECUrlType
import com.sap.ec.core.url.UrlFactoryApi
import com.sap.ec.event.OnlineSdkEvent
import com.sap.ec.event.SdkEvent
import com.sap.ec.event.ack
import com.sap.ec.mobileengage.action.EventActionFactoryApi
import com.sap.ec.mobileengage.action.models.BasicActionModel
import com.sap.ec.mobileengage.inapp.InAppMessage
import com.sap.ec.mobileengage.inapp.presentation.InAppType
import com.sap.ec.networking.clients.EventBasedClientApi
import com.sap.ec.networking.clients.error.ClientExceptionHandler
import com.sap.ec.networking.clients.event.model.ContentCampaign
import com.sap.ec.networking.clients.event.model.DeviceEventRequestBody
import com.sap.ec.networking.clients.event.model.DeviceEventResponse
import com.sap.ec.networking.clients.event.model.OnEventActionCampaign
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class EventClient(
    private val ecNetworkClient: NetworkClientApi,
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
    private val uuidProvider: UuidProviderApi,
    private val deviceInfoCollector: DeviceInfoCollectorApi,
    private val pageLocationProvider: PageLocationProviderApi
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
                    val url = urlFactory.create(ECUrlType.Event)
                    val requestBody =
                        DeviceEventRequestBody(
                            inAppConfigApi.inAppDnd,
                            sdkEvents.map {
                                (it as SdkEvent.DeviceEvent).toDeviceEvent(
                                    platformCategory = deviceInfoCollector.getPlatformCategory(),
                                    pageLocation = pageLocationProvider.provide()
                                )
                            },
                            requestContext.deviceEventState
                        )
                    val body = json.encodeToString(requestBody)
                    val networkResponse = ecNetworkClient.send(
                        UrlRequest(
                            url,
                            HttpMethod.Post,
                            body
                        )
                    )
                    networkResponse.onSuccess { response ->
                        if (response.status == HttpStatusCode.NoContent) {
                            sdkEvents.ack(eventsDao, sdkLogger)
                            emitResponseEventOnSuccess(sdkEvents, response)
                            return@onEach
                        }
                        val result: DeviceEventResponse = response.body()
                        handleDeviceEventState(result)
                        handleInApp(result.contentCampaigns?.getOrNull(0))  // todo handle all campaigns returned
                        result.actionCampaigns?.let { handleOnEventActionCampaigns(it) }
                        emitResponseEventOnSuccess(sdkEvents, response)
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

    private suspend fun emitResponseEventOnSuccess(sdkEvents: List<SdkEvent>, response: Response) {
        sdkEvents.forEach { event ->
            sdkEventManager.emitEvent(
                SdkEvent.Internal.Sdk.Answer.Response(
                    originId = event.id,
                    Result.success(response)
                )
            )
        }
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
                    currentCoroutineContext().ensureActive()
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

            sdkEventManager.emitEvent(
                SdkEvent.Internal.InApp.Present(
                    inAppMessage = InAppMessage(
                        dismissId = uuidProvider.provide(),
                        type = InAppType.OVERLAY,
                        trackingInfo = message.trackingInfo,
                        content = message.content
                    )
                )
            )
        }
    }
}

