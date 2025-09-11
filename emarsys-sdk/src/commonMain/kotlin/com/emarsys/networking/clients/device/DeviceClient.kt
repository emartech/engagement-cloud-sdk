package com.emarsys.networking.clients.device

import com.emarsys.core.channel.SdkEventManagerApi
import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.device.DeviceInfoCollectorApi
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.url.EmarsysUrlType.REGISTER_DEVICE_INFO
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.event.SdkEvent
import com.emarsys.networking.clients.EventBasedClientApi
import com.emarsys.networking.clients.contact.ContactTokenHandlerApi
import com.emarsys.networking.clients.error.ClientExceptionHandler
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class DeviceClient(
    private val emarsysClient: NetworkClientApi,
    private val clientExceptionHandler: ClientExceptionHandler,
    private val urlFactory: UrlFactoryApi,
    private val deviceInfoCollector: DeviceInfoCollectorApi,
    private val contactTokenHandler: ContactTokenHandlerApi,
    private val sdkEventManager: SdkEventManagerApi,
    private val eventsDao: EventsDaoApi,
    private val applicationScope: CoroutineScope,
    private val sdkLogger: Logger
) : EventBasedClientApi {
    override suspend fun register() {
        applicationScope.launch(start = CoroutineStart.UNDISPATCHED) {
            sdkLogger.debug("Register")
            startEventConsumer()
        }
    }

    private suspend fun startEventConsumer() {
        sdkEventManager.onlineSdkEvents
            .filter { it is SdkEvent.Internal.Sdk.RegisterDeviceInfo }
            .collect {
                try {
                    sdkLogger.debug("DeviceClient - consumeRegisterDeviceInfo")
                    val request = createRequest()
                    val response = emarsysClient.send(
                        request,
                        onNetworkError = { sdkEventManager.emitEvent(it) })
                    if (response.status == HttpStatusCode.OK) {
                        contactTokenHandler.handleContactTokens(response)
                    }
                    sdkEventManager.emitEvent(
                        SdkEvent.Internal.Sdk.Answer.Response(
                            originId = it.id,
                            Result.success(response)
                        )
                    )
                    it.ack(eventsDao, sdkLogger)
                } catch (exception: Exception) {
                    clientExceptionHandler.handleException(
                        exception,
                        "DeviceClient - consumeRegisterDeviceInfo",
                        it
                    )
                    sdkEventManager.emitEvent(
                        SdkEvent.Internal.Sdk.Answer.Response(
                            originId = it.id,
                            Result.failure<Exception>(exception)
                        )
                    )
                }
            }
    }

    private suspend fun createRequest(): UrlRequest {
        val deviceInfoString = deviceInfoCollector.collect()
        val url = urlFactory.create(REGISTER_DEVICE_INFO)
        return UrlRequest(
            url,
            HttpMethod.Post,
            deviceInfoString
        )
    }
}