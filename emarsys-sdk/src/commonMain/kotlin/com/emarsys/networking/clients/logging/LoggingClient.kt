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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

internal class LoggingClient(
    private val emarsysNetworkClient: NetworkClientApi,
    private val urlFactory: UrlFactoryApi,
    private val sdkEventDistributor: SdkEventDistributorApi,
    private val json: Json,
    private val sdkLogger: Logger,
    sdkDispatcher: CoroutineDispatcher,
    private val deviceInfoCollector: DeviceInfoCollectorApi,
) {

    init {
        CoroutineScope(sdkDispatcher).launch(start = CoroutineStart.UNDISPATCHED) {
            startEventConsumer()
        }
    }

    private suspend fun startEventConsumer() {
        sdkEventDistributor.onlineEvents
            .filter { it is SdkEvent.Internal.Sdk.Log || it is SdkEvent.Internal.Sdk.Metric }
            .collect {
                sdkLogger.debug("LoggingClient - consumeLogsAndMetrics")
                val url = urlFactory.create(EmarsysUrlType.LOGGING)
                val logLevel = if (it is SdkEvent.Internal.Sdk.Log) {
                    it.level
                } else {
                    (it as SdkEvent.Internal.Sdk.Metric).level
                }
                val request = UrlRequest(
                    url, HttpMethod.Post,
                    json.encodeToString(
                        listOf(
                            LogRequest(
                                deviceInfo = deviceInfoCollector.collectAsDeviceInfoForLogs(),
                                level = logLevel,
                                message = it.attributes?.get("message")?.jsonPrimitive?.contentOrNull,
                                type = "log_request",
                                url = it.attributes?.get("url")?.jsonPrimitive?.contentOrNull,
                                statusCode = it.attributes?.get("statusCode")?.jsonPrimitive?.contentOrNull,
                                networkingDuration = it.attributes?.get("networkingDuration")?.jsonPrimitive?.contentOrNull,
                                networkingEnd = it.attributes?.get("networkingEnd")?.jsonPrimitive?.contentOrNull,
                                networkingStart = it.attributes?.get("networkingStart")?.jsonPrimitive?.contentOrNull,
                                inDbDuration = it.attributes?.get("inDbDuration")?.jsonPrimitive?.contentOrNull,
                                inDbEnd = it.attributes?.get("inDbEnd")?.jsonPrimitive?.contentOrNull,
                                inDbStart = it.attributes?.get("inDbStart")?.jsonPrimitive?.contentOrNull,
                                loadingTimeDuration = it.attributes?.get("loadingTimeDuration")?.jsonPrimitive?.contentOrNull,
                                loadingTimeEnd = it.attributes?.get("loadingTimeEnd")?.jsonPrimitive?.contentOrNull,
                                loadingTimeStart = it.attributes?.get("loadingTimeStart")?.jsonPrimitive?.contentOrNull,
                                onScreenDuration = it.attributes?.get("onScreenDuration")?.jsonPrimitive?.contentOrNull,
                                onScreenEnd = it.attributes?.get("onScreenEnd")?.jsonPrimitive?.contentOrNull,
                                onScreenStart = it.attributes?.get("onScreenStart")?.jsonPrimitive?.contentOrNull,
                                exception = it.attributes?.get("exception")?.jsonPrimitive?.contentOrNull,
                                reason = it.attributes?.get("reason")?.jsonPrimitive?.contentOrNull,
                                stackTrace = it.attributes?.get("stackTrace")?.jsonPrimitive?.contentOrNull,
                                breadcrumbs = it.attributes?.get("breadcrumbs") as JsonObject?,
                            )
                        )
                    )
                )
                emarsysNetworkClient.send(request)
            }
    }
}