package com.emarsys.networking.clients.remoteConfig

import com.emarsys.core.channel.SdkEventManagerApi
import com.emarsys.core.crypto.CryptoApi
import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.event.OnlineSdkEvent
import com.emarsys.event.SdkEvent
import com.emarsys.event.SdkEvent.Internal.Sdk.Answer.Response
import com.emarsys.networking.clients.EventBasedClientApi
import com.emarsys.networking.clients.error.ClientExceptionHandler
import com.emarsys.remoteConfig.RemoteConfigResponse
import com.emarsys.remoteConfig.RemoteConfigResponseHandlerApi
import io.ktor.http.HttpMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.time.ExperimentalTime


@OptIn(ExperimentalTime::class)
internal class RemoteConfigClient(
    private val networkClient: NetworkClientApi,
    private val clientExceptionHandler: ClientExceptionHandler,
    private val urlFactoryApi: UrlFactoryApi,
    private val sdkEventManager: SdkEventManagerApi,
    private val remoteConfigResponseHandler: RemoteConfigResponseHandlerApi,
    private val applicationScope: CoroutineScope,
    private val eventsDao: EventsDaoApi,
    private val crypto: CryptoApi,
    private val json: Json,
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
            .filter { isRemoteConfigEvent(it) }
            .collect {
                try {
                    sdkLogger.debug("ConsumeRemoteConfigEvents")
                    val remoteConfigResponse = fetchRemoteConfig(it)
                    remoteConfigResponse?.let {
                        remoteConfigResponseHandler.handle(remoteConfigResponse)
                    }
                    sdkEventManager.emitEvent(
                        Response(
                            originId = it.id,
                            Result.success(remoteConfigResponse)
                        )
                    )
                    it.ack(eventsDao, sdkLogger)
                } catch (exception: Exception) {
                    handleException(exception, it)
                }
            }
    }

    private suspend fun handleException(exception: Throwable, event: OnlineSdkEvent) {
        clientExceptionHandler.handleException(
            exception,
            "RemoteConfigClient: ConsumeRemoteConfigEvents error",
            event
        )

        sdkEventManager.emitEvent(
            Response(
                originId = event.id,
                Result.failure<Exception>(exception)
            )
        )
    }

    private fun isRemoteConfigEvent(sdkEvent: SdkEvent): Boolean {
        return sdkEvent is SdkEvent.Internal.Sdk.ApplyGlobalRemoteConfig || sdkEvent is SdkEvent.Internal.Sdk.ApplyAppCodeBasedRemoteConfig
    }

    private suspend fun fetchRemoteConfig(event: OnlineSdkEvent): RemoteConfigResponse? {
        val isGlobal = event is SdkEvent.Internal.Sdk.ApplyGlobalRemoteConfig
        sdkLogger.debug(
            "FetchRemoteConfig",
            buildJsonObject { put("global", JsonPrimitive(isGlobal)) })
        return coroutineScope {
            val toBeConfigBytes =
                async(start = CoroutineStart.UNDISPATCHED) { fetchConfig(isGlobal, event) }
            val toBeSignatureBytes =
                async(start = CoroutineStart.UNDISPATCHED) { fetchSignature(isGlobal, event) }
            val configResponse = toBeConfigBytes.await()
            val signatureResponse = toBeSignatureBytes.await()

            val config = configResponse.getOrElse(onFailure = { exception ->
                handleException(exception, event)
                sdkEventManager.emitEvent(
                    event
                )
                null
            })?.bodyAsText
            val signature = signatureResponse.getOrElse(onFailure = { exception ->
                handleException(exception, event)
                sdkEventManager.emitEvent(
                    event
                )
                null
            })?.bodyAsText
            if (config == null || signature == null) {
                return@coroutineScope null
            }
            val verified = crypto.verify(config, signature)
            if (verified) {
                json.decodeFromString(config)
            } else {
                null
            }
        }
    }

    private suspend fun fetchConfig(
        global: Boolean,
        event: OnlineSdkEvent
    ): Result<com.emarsys.core.networking.model.Response> {
        val request = UrlRequest(
            urlFactoryApi.create(
                if (global) EmarsysUrlType.GlobalRemoteConfig else EmarsysUrlType.RemoteConfig
            ),
            HttpMethod.Get
        )
        return executeRequest(request, event)
    }

    private suspend fun fetchSignature(
        global: Boolean,
        event: OnlineSdkEvent
    ): Result<com.emarsys.core.networking.model.Response> {
        val request = UrlRequest(
            urlFactoryApi.create(
                if (global) EmarsysUrlType.GlobalRemoteConfigSignature else EmarsysUrlType.RemoteConfigSignature
            ),
            HttpMethod.Get
        )
        return executeRequest(request, event)
    }

    private suspend fun executeRequest(
        request: UrlRequest,
        event: OnlineSdkEvent
    ): Result<com.emarsys.core.networking.model.Response> {
        return networkClient.send(request)
    }

}