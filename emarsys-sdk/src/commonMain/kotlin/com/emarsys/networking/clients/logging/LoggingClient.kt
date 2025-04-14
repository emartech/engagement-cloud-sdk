package com.emarsys.networking.clients.logging

import com.emarsys.core.channel.SdkEventManagerApi
import com.emarsys.core.channel.batched
import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.device.DeviceInfoCollectorApi
import com.emarsys.core.exceptions.FailedRequestException
import com.emarsys.core.exceptions.MissingApplicationCodeException
import com.emarsys.core.exceptions.RetryLimitReachedException
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.networking.clients.EventBasedClientApi
import com.emarsys.networking.clients.event.model.SdkEvent
import io.ktor.http.HttpMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put
import kotlin.coroutines.coroutineContext

internal class LoggingClient(
    private val genericNetworkClient: NetworkClientApi,
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

    private suspend fun startEventConsumer() {
        sdkEventManager.logEvents
            .batched(batchSize = batchSize, batchIntervalMillis = 10000L)
            .collect { sdkEvents ->
                try {
                    sdkLogger.debug("consumeLogsAndMetrics", isRemoteLog = false)
                    val url = urlFactory.create(EmarsysUrlType.LOGGING)
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
                                    sdkEvent.attributes?.forEach { attribute ->
                                        put(attribute.key, attribute.value)
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
                    genericNetworkClient.send(
                        request,
                        onNetworkError = { sdkEvents.forEach { sdkEventManager.emitEvent(it) } }
                    )
                    sdkEvents.forEach { it.ack(eventsDao, sdkLogger) }
                } catch (exception: Exception) {
                    coroutineContext.ensureActive()
                    when (exception) {
                        is FailedRequestException, is RetryLimitReachedException, is MissingApplicationCodeException -> {
                            sdkEvents.forEach { it.ack(eventsDao, sdkLogger) }
                        }

                        else -> sdkLogger.error(
                            "consumeLogsAndMetrics error",
                            exception,
                            isRemoteLog = false
                        )
                    }
                }
            }
    }
}
