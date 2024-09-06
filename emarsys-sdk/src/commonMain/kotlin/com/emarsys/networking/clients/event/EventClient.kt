package com.emarsys.networking.clients.event

import com.emarsys.api.inapp.InAppConfig
import com.emarsys.core.channel.DeviceEventChannelApi
import com.emarsys.core.channel.naturalBatching
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
import com.emarsys.networking.clients.event.model.Event
import com.emarsys.networking.clients.event.model.EventResponseInApp
import com.emarsys.networking.clients.event.model.EventType
import io.ktor.http.HttpMethod
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class EventClient(
    private val emarsysNetworkClient: NetworkClientApi,
    private val urlFactory: UrlFactoryApi,
    private val json: Json,
    private val deviceEventChannel: DeviceEventChannelApi,
    private val onEventActionFactory: ActionFactoryApi<ActionModel>,
    private val sessionContext: SessionContext,
    private val inAppConfig: InAppConfig,
    private val inAppPresenter: InAppPresenterApi,
    private val inAppViewProvider: InAppViewProviderApi,
    sdkDispatcher: CoroutineDispatcher
) : EventClientApi {

    init {
        CoroutineScope(sdkDispatcher).launch {
            startEventConsumer()
        }
    }

    override suspend fun registerEvent(event: Event) {
        deviceEventChannel.send(event)
    }

    private suspend fun startEventConsumer() {
        deviceEventChannel.consume().naturalBatching().onEach {
            try {
                val url = urlFactory.create(EmarsysUrlType.EVENT)
                val requestBody =
                    DeviceEventRequestBody(
                        inAppConfig.inAppDnd,
                        it,
                        sessionContext.deviceEventState
                    )
                val body = json.encodeToString(requestBody)
                val result: DeviceEventResponse =
                    emarsysNetworkClient.send(UrlRequest(url, HttpMethod.Post, body)).body()
                handleDeviceEventState(result)
                handleInApp(result.message)
                handleOnAppEventAction(result)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.collect {
        }
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
        registerEvent(Event(EventType.INTERNAL, "inapp:viewed", mapOf("campaignId" to campaignId)))
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

