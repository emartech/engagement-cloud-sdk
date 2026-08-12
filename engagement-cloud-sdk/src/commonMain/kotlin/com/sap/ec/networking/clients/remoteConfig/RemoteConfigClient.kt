package com.sap.ec.networking.clients.remoteConfig

import com.sap.ec.core.channel.SdkEventManagerApi
import com.sap.ec.core.crypto.CryptoApi
import com.sap.ec.core.db.events.EventsDaoApi
import com.sap.ec.core.exceptions.SdkException
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
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
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
            .collect { event ->
                try {
                    sdkLogger.debug("ConsumeRemoteConfigEvents")
                    val remoteConfigResponse = fetchRemoteConfig(event)
                    remoteConfigResponse?.let {
                        remoteConfigResponseHandler.handle(remoteConfigResponse)
                        sdkEventManager.emitEvent(
                            Response(
                                originId = event.id,
                                Result.success(remoteConfigResponse)
                            )
                        )
                        event.ack(eventsDao, sdkLogger)
                    }
                } catch (exception: Exception) {
                    handleException(exception, event)
                }
            }
    }

    private suspend fun handleException(exception: Throwable, event: OnlineSdkEvent) {
        currentCoroutineContext().ensureActive()
        clientExceptionHandler.handleException(
            exception,
            "RemoteConfigClient: ConsumeRemoteConfigEvents error",
            event
        )

        // TEMPORARY FIX UNTIL REMOTE CONFIG UPLOAD IS AUTOMATED FOR NEW APP CODES
        if (event is SdkEvent.Internal.Sdk.ApplyAppCodeBasedRemoteConfig &&
            exception is SdkException.FailedRequestException &&
            exception.response.status == HttpStatusCode.NotFound
        ) {
            sdkLogger.debug("Remote config for app code not found.")
            sdkEventManager.emitEvent(
                Response(
                    originId = event.id,
                    Result.success(Unit)
                )
            )
        } else {
            sdkEventManager.emitEvent(
                Response(
                    originId = event.id,
                    Result.failure<Exception>(exception)
                )
            )
        }
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
                async { fetchConfig(isGlobal, event) }
            val toBeSignatureBytes =
                async { fetchSignature(isGlobal, event) }

            val configResponse = toBeConfigBytes.await()
            val signatureResponse = toBeSignatureBytes.await()

            val config = configResponse.getOrNull()?.bodyAsText
            val signature = signatureResponse.getOrNull()?.bodyAsText

            if (config == null || signature == null) {
                val exception =
                    (configResponse.exceptionOrNull() ?: signatureResponse.exceptionOrNull())!!
                handleException(exception, event)
                return@coroutineScope null
            }
            val verified = crypto.verify(config, signature)
            if (verified) {
                json.decodeFromString(config)
            } else {
                event.ack(eventsDao, sdkLogger)
                sdkEventManager.emitEvent(
                    Response(
                        originId = event.id,
                        Result.failure<Unit>(RuntimeException("Remote config signature verification failed"))
                    )
                )
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