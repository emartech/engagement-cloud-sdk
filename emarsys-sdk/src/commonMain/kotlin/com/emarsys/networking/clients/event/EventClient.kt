package com.emarsys.networking.clients.event

import com.emarsys.context.SdkContextApi
import com.emarsys.core.channel.DeviceEventChannelApi
import com.emarsys.core.channel.naturalBatching
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.networking.model.body
import com.emarsys.networking.clients.event.model.DeviceEventRequestBody
import com.emarsys.networking.clients.event.model.DeviceEventResponse
import com.emarsys.networking.clients.event.model.Event
import com.emarsys.session.SessionContext
import com.emarsys.url.EmarsysUrlType
import com.emarsys.url.UrlFactoryApi
import io.ktor.http.HttpMethod
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class EventClient(
    private val emarsysNetworkClient: NetworkClientApi,
    private val urlFactory: UrlFactoryApi,
    private val json: Json,
    private val deviceEventChannel: DeviceEventChannelApi,
    private val sessionContext: SessionContext,
    private val sdkContext: SdkContextApi,
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
        deviceEventChannel.consume().naturalBatching().collect {
            val url = urlFactory.create(EmarsysUrlType.EVENT)
            val requestBody = DeviceEventRequestBody(sdkContext.inAppDndD, it, sessionContext.deviceEventState)
            val body = json.encodeToString(requestBody)
            val result: DeviceEventResponse =
                emarsysNetworkClient.send(UrlRequest(url, HttpMethod.Post, body)).body()
            // handleInapp(result)
            // handleOnAppEventAction(result)
            // handleDeviceEventState(result)
        }
    }
}