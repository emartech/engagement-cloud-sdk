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
import com.sap.ec.recommendation.models.product.Product
import com.sap.ec.recommendation.store.RecommendationCartStorageUpdaterApi
import com.sap.ec.recommendation.RecommendationConstants.CART_LIST_ITEM_QUANTITY_KEY
import com.sap.ec.recommendation.models.FeatureRecommendation
import com.sap.ec.recommendation.models.RecommendationResult
import com.sap.ec.recommendation.models.requestRecommendation.RecommendationLogic
import com.sap.ec.recommendation.models.requestRecommendation.RecommendationOptions
import com.sap.ec.recommendation.networking.RecommendationRequestFactoryApi
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture.Companion.slot
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
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
    private lateinit var mockRecommendationResponseMapper: RecommendationResponseMapperApi
    private lateinit var mockRecommendationCartStorageUpdater: RecommendationCartStorageUpdaterApi

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
        mockRecommendationResponseMapper = mock(MockMode.autofill)
        mockRecommendationCartStorageUpdater = mock(MockMode.autofill)

        sdkEventsFlow = MutableSharedFlow(replay = 100, extraBufferCapacity = Channel.UNLIMITED)
        onlineSdkEventsFlow =
            MutableSharedFlow(replay = 100, extraBufferCapacity = Channel.UNLIMITED)

        everySuspend { mockSdkLogger.debug(any<String>()) }
        everySuspend { mockSdkEventManager.sdkEventFlow } returns sdkEventsFlow
        everySuspend { mockSdkEventManager.onlineSdkEvents } returns onlineSdkEventsFlow
    }

    @Test
    fun testConsumer_shouldConsumeRecommendationTrackEvent_andSendTheRequest_andCallRecommendationCartStorageUpdater_andAckEvent_whenReceivingSuccessResponse() =
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
                mockRecommendationCartStorageUpdater.updateFromEvent(searchEvent)
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
    fun testConsumer_shouldConsumeRecommendationTrackEvent_andSendTheRequest_andNotCallRecommendationCartStorageUpdater_andAckEvent_whenReceivingFailureResponse() =
        runTest {
            createRecommendationClient(backgroundScope).register()
            val searchEvent = SdkEvent.External.RecommendationTrackEvent.Search("testSearchTerm")
            val request = UrlRequest(
                url = Url("$RECOMMENDATION_BASE_URL?$CART_LIST_ITEM_QUANTITY_KEY=testSearchTerm"),
                method = HttpMethod.Get
            )
            val failureResponse = Response(
                status = HttpStatusCode.Unauthorized,
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
            verifySuspend(VerifyMode.exactly(0)) {
                mockRecommendationCartStorageUpdater.updateFromEvent(
                    any<SdkEvent.External.RecommendationTrackEvent.Search>()
                )
            }
        }

    @Test
    fun testConsumer_shouldConsumeRequestRecommendation_andSendTheRequest_andCallRecommendationCartStorageUpdater_andMapResponse_andEmitResultSuccessProductList_whenReceivingSuccessResponse() =
        runTest {
            createRecommendationClient(backgroundScope).register()
            val requestRecommendation = SdkEvent.Internal.Sdk.RequestRecommendation(
                options = RecommendationOptions(
                    RecommendationLogic.Home
                )
            )
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
            val expectedProductResponse = RecommendationResult(
                mapOf(
                    "HOME_1" to FeatureRecommendation(
                        featureName = "HOME_1",
                        topicLabel = null,
                        hasMore = false,
                        merchants = listOf("testMerchants"),
                        products = listOf(
                            Product(
                                "testId",
                                "testTitle",
                                "https://www.test.com"
                            )
                        )
                    )
                )
            )
            val responseCaptor = slot<SdkEvent.Internal.Sdk.Answer.Response<*>>()
            everySuspend { mockRecommendationRequestFactory.create(requestRecommendation) } returns request
            everySuspend { mockNetworkClient.send(request) } returns successResult
            everySuspend { mockRecommendationResponseMapper.map(successResponse) } returns expectedProductResponse
            everySuspend { mockSdkEventManager.emitEvent(capture(responseCaptor)) } returns Unit

            val sdkEvents = backgroundScope.async {
                sdkEventsFlow.take(1).toList()
            }

            sdkEventsFlow.emit(requestRecommendation)

            advanceUntilIdle()

            sdkEvents.await().size shouldBe 1
            val sentEvent = responseCaptor.get()
            sentEvent.result.getOrNull() shouldBe expectedProductResponse
            verifySuspend {
                mockSdkLogger.debug("consume RequestRecommendation events")
                mockRecommendationRequestFactory.create(requestRecommendation)
                mockNetworkClient.send(request)
                mockRecommendationCartStorageUpdater.updateFromEvent(requestRecommendation)
                mockRecommendationResponseMapper.map(successResponse)
                mockSdkEventManager.emitEvent(any())
            }
        }

    @Test
    fun testConsumer_shouldConsumeRequestRecommendation_andSendTheRequest_andNotCallRecommendationCartStorageUpdater_andEmitResponse_whenReceivingFailureResponse() =
        runTest {
            createRecommendationClient(backgroundScope).register()
            val requestRecommendation = SdkEvent.Internal.Sdk.RequestRecommendation(
                options = RecommendationOptions(
                    RecommendationLogic.Home
                )
            )
            val request = UrlRequest(
                url = Url(RECOMMENDATION_BASE_URL),
                method = HttpMethod.Get
            )
            val failureResponse = Response(
                status = HttpStatusCode.Unauthorized,
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
            verifySuspend(VerifyMode.exactly(0)) {
                mockRecommendationCartStorageUpdater.updateFromEvent(
                    any<SdkEvent.Internal.Sdk.RequestRecommendation>()
                )
            }
        }

    @Test
    fun testConsumer_shouldConsumeRequestRecommendation_and_RecommendationTrackEvent() =
        runTest {
            createRecommendationClient(backgroundScope).register()
            val requestRecommendation = SdkEvent.Internal.Sdk.RequestRecommendation(
                options = RecommendationOptions(
                    RecommendationLogic.Home
                )
            )
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

    @Test
    fun testHandleFailure_trackEvent_shouldEmitFailureResponse_whenReceiving400Response() =
        runTest {
            createRecommendationClient(backgroundScope).register()
            val searchEvent = SdkEvent.External.RecommendationTrackEvent.Search("testSearchTerm")
            val request = UrlRequest(
                url = Url("$RECOMMENDATION_BASE_URL?$CART_LIST_ITEM_QUANTITY_KEY=testSearchTerm"),
                method = HttpMethod.Get
            )
            val response = Response(
                status = HttpStatusCode.BadRequest,
                headers = Headers.Empty,
                originalRequest = request,
                bodyAsText = "{}"
            )

            val exception = SdkException.FailedRequestException(response)
            val failureResult = Result.failure<Response>(exception)
            val responseCaptor = slot<SdkEvent.Internal.Sdk.Answer.Response<*>>()
            everySuspend { mockRecommendationRequestFactory.create(searchEvent) } returns request
            everySuspend { mockNetworkClient.send(request) } returns failureResult
            everySuspend { mockSdkEventManager.emitEvent(capture(responseCaptor)) } returns Unit

            val sdkEvents = backgroundScope.async {
                onlineSdkEventsFlow.take(1).toList()
            }

            onlineSdkEventsFlow.emit(searchEvent)

            advanceUntilIdle()

            sdkEvents.await().size shouldBe 1
            val sentEvent = responseCaptor.get()
            val result = sentEvent.result
            result.exceptionOrNull().shouldBeInstanceOf<SdkException.InvalidRecommendationConfigurationException>()
            verifySuspend {
                mockSdkEventManager.emitEvent(
                    SdkEvent.Internal.Sdk.Answer.Response(
                        searchEvent.id,
                        result
                    )
                )
            }
        }

    @Test
    fun testHandleFailure_trackEvent_shouldEmitFailureResponse_whenReceiving401Response() =
        runTest {
            createRecommendationClient(backgroundScope).register()
            val searchEvent = SdkEvent.External.RecommendationTrackEvent.Search("testSearchTerm")
            val request = UrlRequest(
                url = Url("$RECOMMENDATION_BASE_URL?$CART_LIST_ITEM_QUANTITY_KEY=testSearchTerm"),
                method = HttpMethod.Get
            )
            val response = Response(
                status = HttpStatusCode.Unauthorized,
                headers = Headers.Empty,
                originalRequest = request,
                bodyAsText = "{}"
            )

            val exception = SdkException.FailedRequestException(response)
            val failureResult = Result.failure<Response>(exception)
            everySuspend { mockRecommendationRequestFactory.create(searchEvent) } returns request
            everySuspend { mockNetworkClient.send(request) } returns failureResult

            val sdkEvents = backgroundScope.async {
                onlineSdkEventsFlow.take(1).toList()
            }

            onlineSdkEventsFlow.emit(searchEvent)

            advanceUntilIdle()

            sdkEvents.await().size shouldBe 1
            verifySuspend {
                mockSdkEventManager.emitEvent(
                    SdkEvent.Internal.Sdk.Answer.Response(
                        searchEvent.id,
                        failureResult
                    )
                )
            }
        }

    @Test
    fun testHandleFailure_trackEvent_shouldEmitFailureResponse_whenReceiving404Response() =
        runTest {
            createRecommendationClient(backgroundScope).register()
            val searchEvent = SdkEvent.External.RecommendationTrackEvent.Search("testSearchTerm")
            val request = UrlRequest(
                url = Url("$RECOMMENDATION_BASE_URL?$CART_LIST_ITEM_QUANTITY_KEY=testSearchTerm"),
                method = HttpMethod.Get
            )
            val response = Response(
                status = HttpStatusCode.NotFound,
                headers = Headers.Empty,
                originalRequest = request,
                bodyAsText = "{}"
            )

            val exception = SdkException.FailedRequestException(response)
            val failureResult = Result.failure<Response>(exception)
            val responseCaptor = slot<SdkEvent.Internal.Sdk.Answer.Response<*>>()
            everySuspend { mockRecommendationRequestFactory.create(searchEvent) } returns request
            everySuspend { mockNetworkClient.send(request) } returns failureResult
            everySuspend { mockSdkEventManager.emitEvent(capture(responseCaptor)) } returns Unit

            val sdkEvents = backgroundScope.async {
                onlineSdkEventsFlow.take(1).toList()
            }

            onlineSdkEventsFlow.emit(searchEvent)

            advanceUntilIdle()

            sdkEvents.await().size shouldBe 1
            val sentEvent = responseCaptor.get()
            val result = sentEvent.result
            result.exceptionOrNull().shouldBeInstanceOf<SdkException.MissingRecommendationConfigurationException>()
            verifySuspend {
                mockSdkEventManager.emitEvent(
                    SdkEvent.Internal.Sdk.Answer.Response(
                        searchEvent.id,
                        result
                    )
                )
            }
        }

    @Test
    fun testHandleFailure_trackEvent_shouldEmitFailureResponse_whenReceiving500Response() =
        runTest {
            createRecommendationClient(backgroundScope).register()
            val searchEvent = SdkEvent.External.RecommendationTrackEvent.Search("testSearchTerm")
            val request = UrlRequest(
                url = Url("$RECOMMENDATION_BASE_URL?$CART_LIST_ITEM_QUANTITY_KEY=testSearchTerm"),
                method = HttpMethod.Get
            )
            val response = Response(
                status = HttpStatusCode.InternalServerError,
                headers = Headers.Empty,
                originalRequest = request,
                bodyAsText = "{}"
            )

            val exception = SdkException.FailedRequestException(response)
            val failureResult = Result.failure<Response>(exception)
            everySuspend { mockRecommendationRequestFactory.create(searchEvent) } returns request
            everySuspend { mockNetworkClient.send(request) } returns failureResult

            val sdkEvents = backgroundScope.async {
                onlineSdkEventsFlow.take(1).toList()
            }

            onlineSdkEventsFlow.emit(searchEvent)

            advanceUntilIdle()

            sdkEvents.await().size shouldBe 1
            verifySuspend {
                mockSdkEventManager.emitEvent(
                    SdkEvent.Internal.Sdk.Answer.Response(
                        searchEvent.id,
                        failureResult
                    )
                )
            }
        }

    @Test
    fun testHandleFailure_trackEvent_shouldEmitFailureResponse_whenReceivingUnknownFailedRequestExceptionResponse() =
        runTest {
            createRecommendationClient(backgroundScope).register()
            val searchEvent = SdkEvent.External.RecommendationTrackEvent.Search("testSearchTerm")
            val request = UrlRequest(
                url = Url("$RECOMMENDATION_BASE_URL?$CART_LIST_ITEM_QUANTITY_KEY=testSearchTerm"),
                method = HttpMethod.Get
            )
            val response = Response(
                status = HttpStatusCode.PaymentRequired,
                headers = Headers.Empty,
                originalRequest = request,
                bodyAsText = "{}"
            )

            val exception = SdkException.FailedRequestException(response)
            val failureResult = Result.failure<Response>(exception)
            everySuspend { mockRecommendationRequestFactory.create(searchEvent) } returns request
            everySuspend { mockNetworkClient.send(request) } returns failureResult

            val sdkEvents = backgroundScope.async {
                onlineSdkEventsFlow.take(1).toList()
            }

            onlineSdkEventsFlow.emit(searchEvent)

            advanceUntilIdle()

            sdkEvents.await().size shouldBe 1
            verifySuspend {
                mockSdkEventManager.emitEvent(
                    SdkEvent.Internal.Sdk.Answer.Response(
                        searchEvent.id,
                        failureResult
                    )
                )
            }
        }

    @Test
    fun testHandleFailure_trackEvent_shouldEmitFailureResponse_whenReceivingUnknownErrorResponse() =
        runTest {
            createRecommendationClient(backgroundScope).register()
            val searchEvent = SdkEvent.External.RecommendationTrackEvent.Search("testSearchTerm")
            val request = UrlRequest(
                url = Url("$RECOMMENDATION_BASE_URL?$CART_LIST_ITEM_QUANTITY_KEY=testSearchTerm"),
                method = HttpMethod.Get
            )

            val exception = SdkException.SdkDisabledException("SDK is disabled!")
            val failureResult = Result.failure<Response>(exception)
            everySuspend { mockRecommendationRequestFactory.create(searchEvent) } returns request
            everySuspend { mockNetworkClient.send(request) } returns failureResult

            val sdkEvents = backgroundScope.async {
                onlineSdkEventsFlow.take(1).toList()
            }

            onlineSdkEventsFlow.emit(searchEvent)

            advanceUntilIdle()

            sdkEvents.await().size shouldBe 1
            verifySuspend {
                mockSdkEventManager.emitEvent(
                    SdkEvent.Internal.Sdk.Answer.Response(
                        searchEvent.id,
                        failureResult
                    )
                )
            }
        }

    @Test
    fun testHandleFailure_requestRecommendation_shouldEmitFailureResponse_whenReceiving400Response() =
        runTest {
            createRecommendationClient(backgroundScope).register()
            val requestRecommendation = SdkEvent.Internal.Sdk.RequestRecommendation(options = RecommendationOptions(
                RecommendationLogic.Home))
            val request = UrlRequest(
                url = Url(RECOMMENDATION_BASE_URL),
                method = HttpMethod.Get
            )
            val response = Response(
                status = HttpStatusCode.BadRequest,
                headers = Headers.Empty,
                originalRequest = request,
                bodyAsText = "{}"
            )
            val exception = SdkException.FailedRequestException(response)
            val failureResult = Result.failure<Response>(exception)
            val responseCaptor = slot<SdkEvent.Internal.Sdk.Answer.Response<*>>()
            everySuspend { mockRecommendationRequestFactory.create(requestRecommendation) } returns request
            everySuspend { mockNetworkClient.send(request) } returns failureResult
            everySuspend { mockSdkEventManager.emitEvent(capture(responseCaptor)) } returns Unit

            val sdkEvents = backgroundScope.async {
                sdkEventsFlow.take(1).toList()
            }

            sdkEventsFlow.emit(requestRecommendation)

            advanceUntilIdle()

            sdkEvents.await().size shouldBe 1
            val sentEvent = responseCaptor.get()
            val result = sentEvent.result
            result.exceptionOrNull().shouldBeInstanceOf<SdkException.InvalidRecommendationConfigurationException>()
            verifySuspend {
                mockSdkEventManager.emitEvent(
                    SdkEvent.Internal.Sdk.Answer.Response(
                        requestRecommendation.id,
                        result
                    )
                )
            }
        }

    @Test
    fun testHandleFailure_requestRecommendation_shouldEmitFailureResponse_whenReceiving401Response() =
        runTest {
            createRecommendationClient(backgroundScope).register()
            val requestRecommendation = SdkEvent.Internal.Sdk.RequestRecommendation(options = RecommendationOptions(
                RecommendationLogic.Home))
            val request = UrlRequest(
                url = Url(RECOMMENDATION_BASE_URL),
                method = HttpMethod.Get
            )
            val response = Response(
                status = HttpStatusCode.Unauthorized,
                headers = Headers.Empty,
                originalRequest = request,
                bodyAsText = "{}"
            )
            val exception = SdkException.FailedRequestException(response)
            val failureResult = Result.failure<Response>(exception)
            everySuspend { mockRecommendationRequestFactory.create(requestRecommendation) } returns request
            everySuspend { mockNetworkClient.send(request) } returns failureResult

            val sdkEvents = backgroundScope.async {
                sdkEventsFlow.take(1).toList()
            }

            sdkEventsFlow.emit(requestRecommendation)

            advanceUntilIdle()

            sdkEvents.await().size shouldBe 1
            verifySuspend {
                mockSdkEventManager.emitEvent(
                    SdkEvent.Internal.Sdk.Answer.Response(
                        requestRecommendation.id,
                        failureResult
                    )
                )
            }
        }

    @Test
    fun testHandleFailure_requestRecommendation_shouldLog_andEmitMissingRecommendationConfigurationFailureResponse_whenReceiving404Response() =
        runTest {
            createRecommendationClient(backgroundScope).register()
            val requestRecommendation = SdkEvent.Internal.Sdk.RequestRecommendation(options = RecommendationOptions(
                RecommendationLogic.Home))
            val request = UrlRequest(
                url = Url(RECOMMENDATION_BASE_URL),
                method = HttpMethod.Get
            )
            val response = Response(
                status = HttpStatusCode.NotFound,
                headers = Headers.Empty,
                originalRequest = request,
                bodyAsText = "{}"
            )
            val exception = SdkException.FailedRequestException(response)
            val failureResult = Result.failure<Response>(exception)
            val responseCaptor = slot<SdkEvent.Internal.Sdk.Answer.Response<*>>()
            everySuspend { mockRecommendationRequestFactory.create(requestRecommendation) } returns request
            everySuspend { mockNetworkClient.send(request) } returns failureResult
            everySuspend { mockSdkEventManager.emitEvent(capture(responseCaptor)) } returns Unit

            val sdkEvents = backgroundScope.async {
                sdkEventsFlow.take(1).toList()
            }

            sdkEventsFlow.emit(requestRecommendation)

            advanceUntilIdle()

            sdkEvents.await().size shouldBe 1
            val sentEvent = responseCaptor.get()
            val result = sentEvent.result
            result.exceptionOrNull().shouldBeInstanceOf<SdkException.MissingRecommendationConfigurationException>()
            verifySuspend {
                mockSdkLogger.error("recommendation request failed with 404", exception)
                mockSdkEventManager.emitEvent(
                    SdkEvent.Internal.Sdk.Answer.Response(
                        requestRecommendation.id,
                        result
                    )
                )
            }
        }

    @Test
    fun testHandleFailure_requestRecommendation_shouldEmitFailureResponse_whenReceiving500Response() =
        runTest {
            createRecommendationClient(backgroundScope).register()
            val requestRecommendation = SdkEvent.Internal.Sdk.RequestRecommendation(options = RecommendationOptions(
                RecommendationLogic.Home))
            val request = UrlRequest(
                url = Url(RECOMMENDATION_BASE_URL),
                method = HttpMethod.Get
            )
            val response = Response(
                status = HttpStatusCode.InternalServerError,
                headers = Headers.Empty,
                originalRequest = request,
                bodyAsText = "{}"
            )
            val exception = SdkException.FailedRequestException(response)
            val failureResult = Result.failure<Response>(exception)
            everySuspend { mockRecommendationRequestFactory.create(requestRecommendation) } returns request
            everySuspend { mockNetworkClient.send(request) } returns failureResult

            val sdkEvents = backgroundScope.async {
                sdkEventsFlow.take(1).toList()
            }

            sdkEventsFlow.emit(requestRecommendation)

            advanceUntilIdle()

            sdkEvents.await().size shouldBe 1
            verifySuspend {
                mockSdkEventManager.emitEvent(
                    SdkEvent.Internal.Sdk.Answer.Response(
                        requestRecommendation.id,
                        failureResult
                    )
                )
            }
        }

    @Test
    fun testHandleFailure_requestRecommendation_shouldEmitFailureResponse_whenReceivingUnknownFailedRequestExceptionResponse() =
        runTest {
            createRecommendationClient(backgroundScope).register()
            val requestRecommendation = SdkEvent.Internal.Sdk.RequestRecommendation(options = RecommendationOptions(
                RecommendationLogic.Home))
            val request = UrlRequest(
                url = Url(RECOMMENDATION_BASE_URL),
                method = HttpMethod.Get
            )
            val response = Response(
                status = HttpStatusCode.PaymentRequired,
                headers = Headers.Empty,
                originalRequest = request,
                bodyAsText = "{}"
            )
            val exception = SdkException.FailedRequestException(response)
            val failureResult = Result.failure<Response>(exception)
            everySuspend { mockRecommendationRequestFactory.create(requestRecommendation) } returns request
            everySuspend { mockNetworkClient.send(request) } returns failureResult

            val sdkEvents = backgroundScope.async {
                sdkEventsFlow.take(1).toList()
            }

            sdkEventsFlow.emit(requestRecommendation)

            advanceUntilIdle()

            sdkEvents.await().size shouldBe 1
            verifySuspend {
                mockSdkEventManager.emitEvent(
                    SdkEvent.Internal.Sdk.Answer.Response(
                        requestRecommendation.id,
                        failureResult
                    )
                )
            }
        }

    @Test
    fun testHandleFailure_requestRecommendation_shouldEmitFailureResponse_whenReceivingUnknownErrorResponse() =
        runTest {
            createRecommendationClient(backgroundScope).register()
            val requestRecommendation = SdkEvent.Internal.Sdk.RequestRecommendation(options = RecommendationOptions(
                RecommendationLogic.Home))
            val request = UrlRequest(
                url = Url(RECOMMENDATION_BASE_URL),
                method = HttpMethod.Get
            )
            val exception = SdkException.SdkDisabledException("SDK disabled")
            val failureResult = Result.failure<Response>(exception)
            everySuspend { mockRecommendationRequestFactory.create(requestRecommendation) } returns request
            everySuspend { mockNetworkClient.send(request) } returns failureResult

            val sdkEvents = backgroundScope.async {
                sdkEventsFlow.take(1).toList()
            }

            sdkEventsFlow.emit(requestRecommendation)

            advanceUntilIdle()

            sdkEvents.await().size shouldBe 1
            verifySuspend {
                mockSdkEventManager.emitEvent(
                    SdkEvent.Internal.Sdk.Answer.Response(
                        requestRecommendation.id,
                        failureResult
                    )
                )
            }
        }


    private fun createRecommendationClient(applicationScope: CoroutineScope): RecommendationClient {
        return RecommendationClient(
            mockSdkEventManager,
            applicationScope,
            mockNetworkClient,
            mockRecommendationRequestFactory,
            mockRecommendationResponseMapper,
            mockEventsDao,
            mockRecommendationCartStorageUpdater,
            mockSdkLogger
        )
    }
}