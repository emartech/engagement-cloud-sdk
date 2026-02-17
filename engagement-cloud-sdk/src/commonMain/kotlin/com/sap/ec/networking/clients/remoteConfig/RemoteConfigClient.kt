package com.sap.ec.networking.clients.remoteConfig

import com.sap.ec.core.channel.SdkEventManagerApi
import com.sap.ec.core.crypto.CryptoApi
import com.sap.ec.core.db.events.EventsDaoApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.clients.NetworkClientApi
import com.sap.ec.core.networking.model.UrlRequest
import com.sap.ec.core.url.ECUrlType
import com.sap.ec.core.url.UrlFactoryApi
import com.sap.ec.event.OnlineSdkEvent
import com.sap.ec.event.SdkEvent
import com.sap.ec.event.SdkEvent.Internal.Sdk.Answer.Response
import com.sap.ec.networking.clients.EventBasedClientApi
import com.sap.ec.networking.clients.error.ClientExceptionHandler
import com.sap.ec.remoteConfig.RemoteConfigResponse
import com.sap.ec.remoteConfig.RemoteConfigResponseHandlerApi
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
    ): Result<com.sap.ec.core.networking.model.Response> {
        val request = UrlRequest(
            urlFactoryApi.create(
                if (global) ECUrlType.GlobalRemoteConfig else ECUrlType.RemoteConfig
            ),
            HttpMethod.Get
        )
        return executeRequest(request, event)
    }

    private suspend fun fetchSignature(
        global: Boolean,
        event: OnlineSdkEvent
    ): Result<com.sap.ec.core.networking.model.Response> {
        val request = UrlRequest(
            urlFactoryApi.create(
                if (global) ECUrlType.GlobalRemoteConfigSignature else ECUrlType.RemoteConfigSignature
            ),
            HttpMethod.Get
        )
        return executeRequest(request, event)
    }

    private suspend fun executeRequest(
        request: UrlRequest,
        event: OnlineSdkEvent
    ): Result<com.sap.ec.core.networking.model.Response> {
        return networkClient.send(request)
    }

}