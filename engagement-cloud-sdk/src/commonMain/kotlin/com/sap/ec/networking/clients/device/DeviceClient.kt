package com.sap.ec.networking.clients.device

import com.sap.ec.core.channel.SdkEventManagerApi
import com.sap.ec.core.db.events.EventsDaoApi
import com.sap.ec.core.device.DeviceInfoCollectorApi
import com.sap.ec.core.device.DeviceInfoUpdaterApi
import com.sap.ec.core.exceptions.SdkException.NetworkIOException
import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.clients.NetworkClientApi
import com.sap.ec.core.networking.model.UrlRequest
import com.sap.ec.core.url.ECUrlType
import com.sap.ec.core.url.UrlFactoryApi
import com.sap.ec.event.OnlineSdkEvent
import com.sap.ec.event.SdkEvent
import com.sap.ec.networking.clients.EventBasedClientApi
import com.sap.ec.networking.clients.contact.ContactTokenHandlerApi
import com.sap.ec.networking.clients.error.ClientExceptionHandler
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class DeviceClient(
    private val ecClient: NetworkClientApi,
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
                        val networkResponse = ecClient.send(request)
                        networkResponse.onSuccess { response ->
                            if (response.status == HttpStatusCode.OK) {
                                contactTokenHandler.handleContactTokens(response)
                            }
                            sdkEventManager.emitEvent(
                                SdkEvent.Internal.Sdk.Answer.Response(
                                    originId = sdkEvent.id,
                                    Result.success(Unit)
                                )
                            )
                            deviceInfoUpdater.storeDeviceInfo(deviceInfo)
                            sdkEvent.ack(eventsDao, sdkLogger)
                        }
                        networkResponse.onFailure { exception ->
                            handleException(exception, sdkEvent)
                        }
                    } else {
                        sdkLogger.debug("DeviceInfo has not changed.")
                        sdkEvent.ack(eventsDao, sdkLogger)
                        sdkEventManager.emitEvent(
                            SdkEvent.Internal.Sdk.Answer.Response(
                                originId = sdkEvent.id,
                                Result.success(Unit)
                            )
                        )
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
        val url = urlFactory.create(ECUrlType.RegisterDeviceInfo)
        return UrlRequest(
            url,
            HttpMethod.Post,
            deviceInfoString
        )
    }
}