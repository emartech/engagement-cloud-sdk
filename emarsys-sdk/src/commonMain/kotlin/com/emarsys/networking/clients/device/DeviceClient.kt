package com.emarsys.networking.clients.device

import com.emarsys.core.channel.SdkEventManagerApi
import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.device.DeviceInfoCollectorApi
import com.emarsys.core.device.DeviceInfoUpdaterApi
import com.emarsys.core.exceptions.SdkException.NetworkIOException
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.event.OnlineSdkEvent
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
    private val deviceInfoUpdater: DeviceInfoUpdaterApi,
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
            .collect { sdkEvent ->
                sdkLogger.debug("DeviceClient - consumeRegisterDeviceInfo")
                try {
                    val deviceInfo = deviceInfoCollector.collect()
                    if (deviceInfoUpdater.hasDeviceInfoChanged(deviceInfo)) {
                        val request = createRequest(deviceInfo)
                        val networkResponse = emarsysClient.send(request)
                        networkResponse.onSuccess { response ->
                            if (response.status == HttpStatusCode.OK) {
                                contactTokenHandler.handleContactTokens(response)
                            }
                            sdkEventManager.emitEvent(
                                SdkEvent.Internal.Sdk.Answer.Response(
                                    originId = sdkEvent.id,
                                    Result.success(response)
                                )
                            )
                            deviceInfoUpdater.updateDeviceInfoHash(deviceInfo)
                            sdkEvent.ack(eventsDao, sdkLogger)
                        }
                        networkResponse.onFailure { exception ->
                            handleException(exception, sdkEvent)
                        }
                    } else {
                        sdkLogger.debug("DeviceInfo has not changed.")
                        sdkEvent.ack(eventsDao, sdkLogger)
                    }
                } catch (e: Exception) {
                    handleException(e, sdkEvent)
                }
            }
    }

    private suspend fun handleException(exception: Throwable, sdkEvent: OnlineSdkEvent) {
        if (exception is NetworkIOException) {
            sdkEventManager.emitEvent(sdkEvent)
        } else {
            SdkEvent.Internal.Sdk.Answer.Response(
                originId = sdkEvent.id,
                Result.failure<Exception>(exception)
            )

        }
        clientExceptionHandler.handleException(
            exception,
            "DeviceClient - consumeRegisterDeviceInfo",
            sdkEvent
        )
    }

    private fun createRequest(deviceInfoString: String): UrlRequest {
        val url = urlFactory.create(EmarsysUrlType.RegisterDeviceInfo)
        return UrlRequest(
            url,
            HttpMethod.Post,
            deviceInfoString
        )
    }
}