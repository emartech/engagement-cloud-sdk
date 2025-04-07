package com.emarsys.networking.clients.logging

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.device.DeviceInfoCollectorApi
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
    private val sdkEventDistributor: SdkEventDistributorApi,
    private val json: Json,
    private val sdkLogger: Logger,
    private val applicationScope: CoroutineScope,
    private val deviceInfoCollector: DeviceInfoCollectorApi,
) {

    fun register() {
        applicationScope.launch(start = CoroutineStart.UNDISPATCHED) {
            startEventConsumer()
        }
    }

    private suspend fun startEventConsumer() {
        sdkEventDistributor.onlineSdkEvents
            .filter { it is SdkEvent.Internal.Sdk.Log || it is SdkEvent.Internal.Sdk.Metric }
            .collect {
                sdkLogger.debug("LoggingClient - consumeLogsAndMetrics")
                val url = urlFactory.create(EmarsysUrlType.LOGGING)
                val logLevel = if (it is SdkEvent.Internal.Sdk.Log) {
                    it.level
                } else {
                    (it as SdkEvent.Internal.Sdk.Metric).level
                }
                val logRequestJson = buildJsonObject {
                    put("type", JsonPrimitive("log_request"))
                    put("level", JsonPrimitive(logLevel.name))
                    put(
                        "deviceInfo",
                        JsonPrimitive(json.encodeToString(deviceInfoCollector.collectAsDeviceInfoForLogs()))
                    )
                    it.attributes?.forEach { attribute ->
                        put(attribute.key, attribute.value)
                    }
                }
                val request = UrlRequest(
                    url, HttpMethod.Post,
                    json.encodeToString(
                        listOf(
                            logRequestJson
                        )
                    )
                )
                emarsysNetworkClient.send(request)
            }
    }
}