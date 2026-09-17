package com.sap.ec.networking.clients.recommendation

import com.sap.ec.core.channel.SdkEventManagerApi
import com.sap.ec.core.db.events.EventsDaoApi
import com.sap.ec.core.exceptions.SdkException
import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.clients.NetworkClientApi
import com.sap.ec.core.networking.model.Response
import com.sap.ec.core.networking.model.UrlRequest
import com.sap.ec.event.OnlineSdkEvent
import com.sap.ec.event.SdkEvent
import com.sap.ec.mobileengage.recommendation.RecommendationConstants.CART_LIST_ITEM_QUANTITY_KEY
import com.sap.ec.mobileengage.recommendation.networking.RecommendationRequestFactoryApi
import com.sap.ec.recommendation.RecommendationLogic
import com.sap.ec.recommendation.RecommendationOptions
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecommendationClientTests {

    private lateinit var mockSdkLogger: Logger
    private lateinit var mockSdkEventManager: SdkEventManagerApi
    private lateinit var mockEventsDao: EventsDaoApi
    private lateinit var mockNetworkClient: NetworkClientApi
    private lateinit var mockRecommendationRequestFactory: RecommendationRequestFactoryApi
    private lateinit var sdkEventsFlow: MutableSharedFlow<SdkEvent>
    private lateinit var onlineSdkEventsFlow: MutableSharedFlow<OnlineSdkEvent>

    private companion object {
        private const val RECOMMENDATION_BASE_URL =
            "https://recommender.scarabresearch.com/merchants/"
    }

    @BeforeTest
    fun setup() = runTest {
        Dispatchers.setMain(StandardTestDispatcher())
        mockSdkLogger = mock(MockMode.autofill)
        mockSdkEventManager = mock(MockMode.autofill)
        mockEventsDao = mock(MockMode.autofill)
        mockNetworkClient = mock(MockMode.autofill)
        mockRecommendationRequestFactory = mock(MockMode.autofill)

        sdkEventsFlow = MutableSharedFlow(replay = 100, extraBufferCapacity = Channel.UNLIMITED)
        onlineSdkEventsFlow = MutableSharedFlow(replay = 100, extraBufferCapacity = Channel.UNLIMITED)

        everySuspend { mockSdkLogger.debug(any<String>()) }
        everySuspend { mockSdkEventManager.sdkEventFlow } returns sdkEventsFlow
        everySuspend { mockSdkEventManager.onlineSdkEvents } returns onlineSdkEventsFlow
    }

    @Test
    fun testConsumer_shouldConsumeRecommendationTrackEvent_and_sendTheRequest_andAckEvent_whenReceivingSuccessResponse() =
        runTest {
            createRecommendationClient(backgroundScope).register()
            val searchEvent = SdkEvent.External.RecommendationTrackEvent.Search("testSearchTerm")
            val request = UrlRequest(
                url = Url("$RECOMMENDATION_BASE_URL?$CART_LIST_ITEM_QUANTITY_KEY=testSearchTerm"),
                method = HttpMethod.Get
            )
            val successResponse = Response(
                status = HttpStatusCode.OK,
                headers = Headers.Empty,
                originalRequest = request,
                bodyAsText = "{}"
            )
            val successResult = Result.success(successResponse)
            everySuspend { mockRecommendationRequestFactory.create(searchEvent) } returns request
            everySuspend { mockNetworkClient.send(request) } returns successResult

            val sdkEvents = backgroundScope.async {
                onlineSdkEventsFlow.take(1).toList()
            }

            onlineSdkEventsFlow.emit(searchEvent)

            advanceUntilIdle()

            sdkEvents.await().size shouldBe 1
            verifySuspend {
                mockSdkLogger.debug("consume RecommendationTrackEvent events")
                mockRecommendationRequestFactory.create(searchEvent)
                mockNetworkClient.send(request)
                mockSdkEventManager.emitEvent(
                    SdkEvent.Internal.Sdk.Answer.Response(
                        searchEvent.id,
                        successResult
                    )
                )
                mockEventsDao.removeEvent(searchEvent)
            }
        }

    @Test
    fun testConsumer_shouldConsumeRecommendationTrackEvent_and_sendTheRequest_andAckEvent_whenReceivingFailureResponse() =
        runTest {
            createRecommendationClient(backgroundScope).register()
            val searchEvent = SdkEvent.External.RecommendationTrackEvent.Search("testSearchTerm")
            val request = UrlRequest(
                url = Url("$RECOMMENDATION_BASE_URL?$CART_LIST_ITEM_QUANTITY_KEY=testSearchTerm"),
                method = HttpMethod.Get
            )
            val failureResponse = Response(
                status = HttpStatusCode.BadRequest,
                headers = Headers.Empty,
                originalRequest = request,
                bodyAsText = "{}"
            )
            val failureResult =
                Result.failure<Response>(SdkException.FailedRequestException(failureResponse))
            everySuspend { mockRecommendationRequestFactory.create(searchEvent) } returns request
            everySuspend { mockNetworkClient.send(request) } returns failureResult

            val sdkEvents = backgroundScope.async {
                onlineSdkEventsFlow.take(1).toList()
            }

            onlineSdkEventsFlow.emit(searchEvent)

            advanceUntilIdle()

            sdkEvents.await().size shouldBe 1
            verifySuspend {
                mockSdkLogger.debug("consume RecommendationTrackEvent events")
                mockRecommendationRequestFactory.create(searchEvent)
                mockNetworkClient.send(request)

                mockSdkEventManager.emitEvent(
                    SdkEvent.Internal.Sdk.Answer.Response(
                        searchEvent.id,
                        failureResult
                    )
                )
                mockEventsDao.removeEvent(searchEvent)
            }
        }

    @Test
    fun testConsumer_shouldConsumeRequestRecommendation_and_sendTheRequest_and_emitResponse_whenReceivingSuccessResponse() =
        runTest {
            createRecommendationClient(backgroundScope).register()
            val requestRecommendation = SdkEvent.Internal.Sdk.RequestRecommendation(options = RecommendationOptions(
                RecommendationLogic.HOME))
            val request = UrlRequest(
                url = Url(RECOMMENDATION_BASE_URL),
                method = HttpMethod.Get
            )
            val successResponse = Response(
                status = HttpStatusCode.OK,
                headers = Headers.Empty,
                originalRequest = request,
                bodyAsText = "{}"
            )
            val successResult = Result.success(successResponse)
            everySuspend { mockRecommendationRequestFactory.create(requestRecommendation) } returns request
            everySuspend { mockNetworkClient.send(request) } returns successResult

            val sdkEvents = backgroundScope.async {
                sdkEventsFlow.take(1).toList()
            }

            sdkEventsFlow.emit(requestRecommendation)

            advanceUntilIdle()

            sdkEvents.await().size shouldBe 1
            verifySuspend {
                mockSdkLogger.debug("consume RequestRecommendation events")
                mockRecommendationRequestFactory.create(requestRecommendation)
                mockNetworkClient.send(request)
                mockSdkEventManager.emitEvent(
                    SdkEvent.Internal.Sdk.Answer.Response(
                        requestRecommendation.id,
                        successResult
                    )
                )
            }
        }

    @Test
    fun testConsumer_shouldConsumeRequestRecommendation_and_sendTheRequest_and_emitResponse_whenReceivingFailureResponse() =
        runTest {
            createRecommendationClient(backgroundScope).register()
            val requestRecommendation = SdkEvent.Internal.Sdk.RequestRecommendation(options = RecommendationOptions(
                RecommendationLogic.HOME))
            val request = UrlRequest(
                url = Url(RECOMMENDATION_BASE_URL),
                method = HttpMethod.Get
            )
            val failureResponse = Response(
                status = HttpStatusCode.BadRequest,
                headers = Headers.Empty,
                originalRequest = request,
                bodyAsText = "{}"
            )
            val failureResult =
                Result.failure<Response>(SdkException.FailedRequestException(failureResponse))
            everySuspend { mockRecommendationRequestFactory.create(requestRecommendation) } returns request
            everySuspend { mockNetworkClient.send(request) } returns failureResult

            val sdkEvents = backgroundScope.async {
                sdkEventsFlow.take(1).toList()
            }

            sdkEventsFlow.emit(requestRecommendation)

            advanceUntilIdle()

            sdkEvents.await().size shouldBe 1
            verifySuspend {
                mockSdkLogger.debug("consume RequestRecommendation events")
                mockRecommendationRequestFactory.create(requestRecommendation)
                mockNetworkClient.send(request)

                mockSdkEventManager.emitEvent(
                    SdkEvent.Internal.Sdk.Answer.Response(
                        requestRecommendation.id,
                        failureResult
                    )
                )
            }
        }

    @Test
    fun testConsumer_shouldConsumeRequestRecommendation_and_RecommendationTrackEvent() =
        runTest {
            createRecommendationClient(backgroundScope).register()
            val requestRecommendation = SdkEvent.Internal.Sdk.RequestRecommendation(options = RecommendationOptions(
                RecommendationLogic.HOME))
            val searchEvent = SdkEvent.External.RecommendationTrackEvent.Search("testSearchTerm")
            val sdkEvents = backgroundScope.async {
                sdkEventsFlow.take(1).toList()
            }
            val sdkOnlineEvents = backgroundScope.async {
                onlineSdkEventsFlow.take(1).toList()
            }

            sdkEventsFlow.emit(requestRecommendation)
            onlineSdkEventsFlow.emit(searchEvent)

            advanceUntilIdle()

            sdkEvents.await().size shouldBe 1
            sdkOnlineEvents.await().size shouldBe 1
            verifySuspend(VerifyMode.exactly(1)) {
                mockRecommendationRequestFactory.create(requestRecommendation)
                mockRecommendationRequestFactory.create(searchEvent)
            }
        }

    private fun createRecommendationClient(applicationScope: CoroutineScope): RecommendationClient {
        return RecommendationClient(
            mockSdkEventManager,
            applicationScope,
            mockNetworkClient,
            mockRecommendationRequestFactory,
            mockEventsDao,
            mockSdkLogger
        )
    }
}