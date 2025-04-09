package com.emarsys.networking.clients.device

import com.emarsys.core.channel.SdkEventManagerApi
import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.device.DeviceInfoCollectorApi
import com.emarsys.core.exceptions.FailedRequestException
import com.emarsys.core.exceptions.MissingApplicationCodeException
import com.emarsys.core.exceptions.RetryLimitReachedException
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.url.EmarsysUrlType.REGISTER_DEVICE_INFO
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.networking.clients.EventBasedClientApi
import com.emarsys.networking.clients.contact.ContactTokenHandlerApi
import com.emarsys.networking.clients.event.model.SdkEvent
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

internal class DeviceClient(
    private val emarsysClient: NetworkClientApi,
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
                    val response = emarsysClient.send(request, onNetworkError =  { sdkEventManager.emitEvent(it) })
                    if (response.status == HttpStatusCode.OK) {
                        contactTokenHandler.handleContactTokens(response)
                    }
                    it.ack(eventsDao, sdkLogger)
                } catch (exception: Exception) {
                    when (exception) {
                        is FailedRequestException, is RetryLimitReachedException, is MissingApplicationCodeException -> it.ack(
                            eventsDao,
                            sdkLogger
                        )

                        else -> sdkLogger.error(
                            "DeviceClient - consumeRegisterDeviceInfo",
                            exception
                        )
                    }
                }
            }
    }

    private suspend fun createRequest(): UrlRequest {
        val deviceInfoString = deviceInfoCollector.collect()
        val url = urlFactory.create(REGISTER_DEVICE_INFO, null)
        return UrlRequest(
            url,
            HttpMethod.Post,
            deviceInfoString
        )
    }
}