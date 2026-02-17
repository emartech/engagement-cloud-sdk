package com.sap.ec.networking.clients.logging

import com.sap.ec.core.channel.SdkEventManagerApi
import com.sap.ec.core.channel.batched
import com.sap.ec.core.db.events.EventsDaoApi
import com.sap.ec.core.device.DeviceInfoCollectorApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.clients.NetworkClientApi
import com.sap.ec.core.networking.model.UrlRequest
import com.sap.ec.core.url.ECUrlType
import com.sap.ec.core.url.UrlFactoryApi
import com.sap.ec.event.SdkEvent
import com.sap.ec.networking.clients.EventBasedClientApi
import com.sap.ec.networking.clients.error.ClientExceptionHandler
import io.ktor.http.HttpMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put
import kotlin.time.ExperimentalTime

internal class LoggingClient(
    private val genericNetworkClient: NetworkClientApi,
    private val clientExceptionHandler: ClientExceptionHandler,
    private val urlFactory: UrlFactoryApi,
    private val sdkEventManager: SdkEventManagerApi,
    private val json: Json,
    private val sdkLogger: Logger,
    private val applicationScope: CoroutineScope,
    private val deviceInfoCollector: DeviceInfoCollectorApi,
    private val eventsDao: EventsDaoApi,
    private val batchSize: Int = 10
) : EventBasedClientApi {

    override suspend fun register() {
        applicationScope.launch(start = CoroutineStart.UNDISPATCHED) {
            sdkLogger.debug("Register")
            startEventConsumer()
        }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun startEventConsumer() {
        sdkEventManager.logEvents
            .batched(batchSize = batchSize, batchIntervalMillis = 10000L)
            .collect { sdkEvents ->
                try {
                    sdkLogger.debug("consumeLogsAndMetrics", isRemoteLog = false)
                    val url = urlFactory.create(ECUrlType.Logging)
                    val logRequestsJson =
                        buildJsonObject {
                            val logRequestJsonArray = sdkEvents.map { sdkEvent ->
                                val logLevel = if (sdkEvent is SdkEvent.Internal.Sdk.Log) {
                                    sdkEvent.level
                                } else {
                                    (sdkEvent as SdkEvent.Internal.Sdk.Metric).level
                                }
                                buildJsonObject {
                                    put("type", "log_request")
                                    put("level", logLevel.name.uppercase())
                                    put(
                                        "deviceInfo",
                                        json.encodeToJsonElement(deviceInfoCollector.collectAsDeviceInfoForLogs())
                                    )
                                    if (sdkEvent is SdkEvent.Internal.Sdk.Log) {
                                        sdkEvent.attributes?.forEach { attribute ->
                                            put(attribute.key, attribute.value)
                                        }
                                    }
                                }
                            }
                            put("logs", JsonArray(logRequestJsonArray))
                        }

                    val request = UrlRequest(
                        url, HttpMethod.Post,
                        json.encodeToString(logRequestsJson),
                        isLogRequest = true
                    )
                    val response = genericNetworkClient.send(
                        request
                    )
                    response.onSuccess {
                        sdkEvents.forEach { it.ack(eventsDao, sdkLogger) }
                    }

                    response.onFailure { exception ->
                        handleException(exception, sdkEvents)
                        sdkEvents.forEach { sdkEvent ->
                            sdkEventManager.emitEvent(sdkEvent)
                        }
                    }
                } catch (exception: Exception) {
                    handleException(exception, sdkEvents)
                }
            }
    }

    private suspend fun handleException(
        exception: Throwable,
        sdkEvents: List<SdkEvent.Internal.LogEvent>
    ) {
        currentCoroutineContext().ensureActive()
        clientExceptionHandler.handleException(
            exception,
            "LoggingClient: ConsumeLogsAndMetrics error",
            *sdkEvents.toTypedArray()
        )
    }
}
