package com.sap.ec.networking.clients.recommendation

import com.sap.ec.core.channel.SdkEventManagerApi
import com.sap.ec.core.db.events.EventsDaoApi
import com.sap.ec.core.exceptions.SdkException
import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.clients.NetworkClientApi
import com.sap.ec.core.networking.model.Response
import com.sap.ec.recommendation.store.RecommendationCartStorageUpdaterApi
import com.sap.ec.event.SdkEvent
import com.sap.ec.networking.clients.EventBasedClientApi
import com.sap.ec.recommendation.networking.RecommendationRequestFactoryApi
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch

internal class RecommendationClient(
    private val sdkEventManager: SdkEventManagerApi,
    private val applicationScope: CoroutineScope,
    private val recommendationNetworkClient: NetworkClientApi,
    private val recommendationRequestFactory: RecommendationRequestFactoryApi,
    private val recommendationResponseMapper: RecommendationResponseMapperApi,
    private val eventsDao: EventsDaoApi,
    private val recommendationCartStorageUpdater: RecommendationCartStorageUpdaterApi,
    private val sdkLogger: Logger
) : EventBasedClientApi {

    override suspend fun register() {
        sdkLogger.debug("register RecommendationClient")
        startRecommendationTrackEventConsumer()
        startRequestRecommendationEventConsumer()
    }

    private fun startRecommendationTrackEventConsumer() {
        applicationScope.launch(start = CoroutineStart.UNDISPATCHED) {
            sdkEventManager.onlineSdkEvents.filterIsInstance<SdkEvent.External.RecommendationTrackEvent>()
                .collect { event ->
                    sdkLogger.debug("consume RecommendationTrackEvent events")
                    val request = recommendationRequestFactory.create(event)
                    recommendationNetworkClient.send(request).fold(
                        onSuccess = { successResponse ->
                            recommendationCartStorageUpdater.updateFromEvent(event)
                            sdkEventManager.emitEvent(
                                SdkEvent.Internal.Sdk.Answer.Response(
                                    event.id,
                                    Result.success(successResponse)
                                )
                            )
                            event.ack(eventsDao, sdkLogger)
                        },
                        onFailure = { exception ->
                            sdkEventManager.emitEvent(
                                SdkEvent.Internal.Sdk.Answer.Response(
                                    event.id,
                                    Result.failure<Response>(exception)
                                )
                            )
                            event.ack(eventsDao, sdkLogger)
                        }
                    )
                }
        }
    }

    private fun startRequestRecommendationEventConsumer() {
        applicationScope.launch(start = CoroutineStart.UNDISPATCHED) {
            sdkEventManager.sdkEventFlow.filterIsInstance<SdkEvent.Internal.Sdk.RequestRecommendation>()
                .collect { event ->
                    sdkLogger.debug("consume RequestRecommendation events")
                    val request = recommendationRequestFactory.create(event)
                    recommendationNetworkClient.send(request).fold(
                        onSuccess = { successResponse ->
                            recommendationCartStorageUpdater.updateFromEvent(event)
                            val recommendationResult = recommendationResponseMapper.map(successResponse)
                            sdkEventManager.emitEvent(
                                SdkEvent.Internal.Sdk.Answer.Response(
                                    event.id,
                                    Result.success(recommendationResult)
                                )
                            )
                        },
                        onFailure = { exception ->
                            handleRequestRecommendationFailure(exception, event)
                        }
                    )
                }
        }
    }

    private suspend fun handleRequestRecommendationFailure(exception: Throwable, event: SdkEvent.Internal.Sdk.RequestRecommendation) {
        if (exception is SdkException.FailedRequestException) {
            when (exception.response.status) {
                HttpStatusCode.BadRequest -> {
                    sdkLogger.error("recommendation request failed with 400", exception)
                    sdkEventManager.emitEvent(
                        SdkEvent.Internal.Sdk.Answer.Response(
                            originId = event.id,
                            Result.failure<Response>(SdkException.InvalidRecommendationConfigurationException())
                        )
                    )
                }
                HttpStatusCode.Unauthorized -> {
                    sdkLogger.debug("recommendation request failed with 401", exception)
                    sdkEventManager.emitEvent(
                        SdkEvent.Internal.Sdk.Answer.Response(
                            originId = event.id,
                            Result.failure<Response>(exception)
                        )
                    )
                }
                HttpStatusCode.NotFound -> {
                    sdkLogger.error("recommendation request failed with 404", exception)
                    sdkEventManager.emitEvent(
                        SdkEvent.Internal.Sdk.Answer.Response(
                            originId = event.id,
                            Result.failure<Response>(SdkException.MissingRecommendationConfigurationException())
                        )
                    )
                }
                HttpStatusCode.InternalServerError -> {
                    sdkLogger.debug("recommendation request failed with 500", exception)
                    sdkEventManager.emitEvent(
                        SdkEvent.Internal.Sdk.Answer.Response(
                            originId = event.id,
                            Result.failure<Response>(exception)
                        )
                    )
                }
                else -> {
                    sdkLogger.debug("recommendation request failed", exception)
                    sdkEventManager.emitEvent(
                        SdkEvent.Internal.Sdk.Answer.Response(
                            originId = event.id,
                            Result.failure<Response>(exception)
                        )
                    )
                }
            }
        } else {
            sdkLogger.debug("recommendation request failed", exception)
            sdkEventManager.emitEvent(
                SdkEvent.Internal.Sdk.Answer.Response(
                    originId = event.id,
                    Result.failure<Exception>(exception)
                )
            )
        }
    }
}