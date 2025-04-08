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
import com.emarsys.networking.clients.event.model.SdkEvent
import io.ktor.http.HttpMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

internal class LoggingClient(
    private val emarsysNetworkClient: NetworkClientApi,
    private val urlFactory: UrlFactoryApi,
    private val sdkEventManager: SdkEventManagerApi,
    private val json: Json,
    private val sdkLogger: Logger,
    private val applicationScope: CoroutineScope,
    private val deviceInfoCollector: DeviceInfoCollectorApi,
    private val eventsDao: EventsDaoApi,
    private val batchSize: Int = 1
) {

    fun register() {
        applicationScope.launch(start = CoroutineStart.UNDISPATCHED) {
            startEventConsumer()
        }
    }

    private suspend fun startEventConsumer() {
        sdkEventManager.onlineSdkEvents
            .filter { it is SdkEvent.Internal.Sdk.Log || it is SdkEvent.Internal.Sdk.Metric }
            .batched(batchSize = batchSize, batchIntervalMillis = 10000L)
            .collect { sdkEvents ->
                try {
                    sdkLogger.debug("LoggingClient - consumeLogsAndMetrics")
                    val url = urlFactory.create(EmarsysUrlType.LOGGING)
                    val logRequestsJson = sdkEvents.map { sdkEvent ->
                        val logLevel = if (sdkEvent is SdkEvent.Internal.Sdk.Log) {
                            sdkEvent.level
                        } else {
                            (sdkEvent as SdkEvent.Internal.Sdk.Metric).level
                        }
                        buildJsonObject {
                            put("type", JsonPrimitive("log_request"))
                            put("level", JsonPrimitive(logLevel.name))
                            put(
                                "deviceInfo",
                                JsonPrimitive(json.encodeToString(deviceInfoCollector.collectAsDeviceInfoForLogs()))
                            )
                            sdkEvent.attributes?.forEach { attribute ->
                                put(attribute.key, attribute.value)
                            }
                        }
                    }
                    val request = UrlRequest(
                        url, HttpMethod.Post,
                        json.encodeToString(logRequestsJson)
                    )
                    emarsysNetworkClient.send(
                        request,
                        onNetworkError = { sdkEvents.forEach { sdkEventManager.emitEvent(it) } }
                    )
                    sdkEvents.forEach { it.ack(eventsDao, sdkLogger) }
                } catch (exception: Exception) {
                    when (exception) {
                        is FailedRequestException, is RetryLimitReachedException, is MissingApplicationCodeException -> {
                            sdkEvents.forEach { it.ack(eventsDao, sdkLogger) }
                        }

                        else -> sdkLogger.error(
                            "LoggingClient - consumeLogsAndMetrics",
                            exception
                        )
                    }
                }
            }
    }
}
