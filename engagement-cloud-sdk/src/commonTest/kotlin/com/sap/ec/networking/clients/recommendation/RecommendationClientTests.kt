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
import com.sap.ec.networking.clients.error.ClientExceptionHandler
import com.sap.ec.recommendation.RecommendationConstants.CART_LIST_ITEM_QUANTITY_KEY
import com.sap.ec.recommendation.models.RecommendationResult
import com.sap.ec.recommendation.models.product.Product
import com.sap.ec.recommendation.models.requestRecommendation.RecommendationLogic
import com.sap.ec.recommendation.models.requestRecommendation.RecommendationOptions
import com.sap.ec.recommendation.networking.RecommendationRequestFactoryApi
import com.sap.ec.recommendation.store.RecommendationCartStorageUpdaterApi
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture.Companion.slot
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
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
import kotlinx.coroutines.test.TestScope
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
    private lateinit var mockClientExceptionHandler: ClientExceptionHandler

    private companion object {
        private const val RECOMMENDATION_BASE_URL =
            "https://recommender.scarabresearch.com/merchants/"

        private val searchEvent = SdkEvent.External.RecommendationTrackEvent.Search("testSearchTerm")

        private val trackRequest = UrlRequest(
            url = Url("$RECOMMENDATION_BASE_URL?$CART_LIST_ITEM_QUANTITY_KEY=testSearchTerm"),
            method = HttpMethod.Get
        )

        private val requestRecommendationUrlRequest = UrlRequest(
            url = Url(RECOMMENDATION_BASE_URL),
            method = HttpMethod.Get
        )

        private val requestRecommendationEvent = SdkEvent.Internal.Sdk.RequestRecommendation(
            options = RecommendationOptions(RecommendationLogic.Home)
        )

        private val successResponse = Response(
            status = HttpStatusCode.OK,
            headers = Headers.Empty,
            originalRequest = trackRequest,
            bodyAsText = "{}"
        )

        private val successResult = Result.success(successResponse)
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
        mockClientExceptionHandler = mock(MockMode.autofill)

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
            everySuspend { mockRecommendationRequestFactory.create(searchEvent) } returns trackRequest
            everySuspend { mockNetworkClient.send(trackRequest) } returns successResult

            emitOnlineAndAwait(searchEvent) shouldBe 1

            verifySuspend {
                mockSdkLogger.debug("consume RecommendationTrackEvent events")
                mockRecommendationRequestFactory.create(searchEvent)
                mockNetworkClient.send(trackRequest)
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
    fun testConsumer_shouldConsumeRecommendationTrackEvent_andSendTheRequest_andNotCallRecommendationCartStorageUpdater_andCallClientExceptionHandler_whenReceivingFailureResponse() =
        runTest {
            createRecommendationClient(backgroundScope).register()
            val failureResponse = Response(
                status = HttpStatusCode.Unauthorized,
                headers = Headers.Empty,
                originalRequest = trackRequest,
                bodyAsText = "{}"
            )
            val exception = SdkException.FailedRequestException(failureResponse)
            val failureResult =
                Result.failure<Response>(exception)
            everySuspend { mockRecommendationRequestFactory.create(searchEvent) } returns trackRequest
            everySuspend { mockNetworkClient.send(trackRequest) } returns failureResult

            emitOnlineAndAwait(searchEvent) shouldBe 1

            verifySuspend {
                mockSdkLogger.debug("consume RecommendationTrackEvent events")
                mockRecommendationRequestFactory.create(searchEvent)
                mockNetworkClient.send(trackRequest)
                mockSdkEventManager.emitEvent(
                    SdkEvent.Internal.Sdk.Answer.Response(
                        searchEvent.id,
                        failureResult
                    )
                )
                mockClientExceptionHandler.handleException(
                    exception,
                    "exception while consuming RecommendationTrackEvent events",
                    searchEvent
                )
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
            val request = UrlRequest(
                url = Url(RECOMMENDATION_BASE_URL),
                method = HttpMethod.Get
            )
            val expectedProductResponse = mapOf(
                "HOME_1" to RecommendationResult(
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
            val responseCaptor = slot<SdkEvent.Internal.Sdk.Answer.Response<*>>()
            everySuspend { mockRecommendationRequestFactory.create(requestRecommendationEvent) } returns request
            everySuspend { mockNetworkClient.send(request) } returns successResult
            everySuspend { mockRecommendationResponseMapper.map(successResponse) } returns expectedProductResponse
            everySuspend { mockSdkEventManager.emitEvent(capture(responseCaptor)) } returns Unit

            emitInternalAndAwait(requestRecommendationEvent) shouldBe 1

            val sentEvent = responseCaptor.get()
            sentEvent.result.getOrNull() shouldBe expectedProductResponse
            verifySuspend {
                mockSdkLogger.debug("consume RequestRecommendation events")
                mockRecommendationRequestFactory.create(requestRecommendationEvent)
                mockNetworkClient.send(request)
                mockRecommendationCartStorageUpdater.updateFromEvent(requestRecommendationEvent)
                mockRecommendationResponseMapper.map(successResponse)
                mockSdkEventManager.emitEvent(any())
            }
        }

    @Test
    fun testConsumer_shouldConsumeRequestRecommendation_andSendTheRequest_andNotCallRecommendationCartStorageUpdater_andEmitResponse_whenReceivingFailureResponse() =
        runTest {
            createRecommendationClient(backgroundScope).register()
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
            everySuspend { mockRecommendationRequestFactory.create(requestRecommendationEvent) } returns request
            everySuspend { mockNetworkClient.send(request) } returns failureResult

            emitInternalAndAwait(requestRecommendationEvent) shouldBe 1

            verifySuspend {
                mockSdkLogger.debug("consume RequestRecommendation events")
                mockRecommendationRequestFactory.create(requestRecommendationEvent)
                mockNetworkClient.send(request)

                mockSdkEventManager.emitEvent(
                    SdkEvent.Internal.Sdk.Answer.Response(
                        requestRecommendationEvent.id,
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
            emitInternalAndAwait(requestRecommendationEvent) shouldBe 1
            emitOnlineAndAwait(searchEvent) shouldBe 1
            verifySuspend(VerifyMode.exactly(1)) {
                mockRecommendationRequestFactory.create(requestRecommendationEvent)
                mockRecommendationRequestFactory.create(searchEvent)
            }
        }

    private data class FailureCase(
        val status: HttpStatusCode,
        val surfacedException: (SdkException.FailedRequestException) -> Throwable,
        val shouldTransformException: Boolean,
        val expectsErrorLog: Boolean = false
    )

    private val failureCases = listOf(
        FailureCase(
            status = HttpStatusCode.BadRequest,
            surfacedException = { SdkException.InvalidRecommendationConfigurationException() },
            shouldTransformException = true
        ),
        FailureCase(
            status = HttpStatusCode.NotFound,
            surfacedException = { SdkException.MissingRecommendationConfigurationException() },
            shouldTransformException = true,
            expectsErrorLog = true
        ),
        FailureCase(
            status = HttpStatusCode.Unauthorized,
            surfacedException = { it },
            shouldTransformException = false
        ),
        FailureCase(
            status = HttpStatusCode.InternalServerError,
            surfacedException = { it },
            shouldTransformException = false
        ),
        FailureCase(
            status = HttpStatusCode.PaymentRequired,
            surfacedException = { it },
            shouldTransformException = false
        )
    )

    @Test
    fun testHandleFailure_trackEvent_shouldSurfaceMappedException_andHandleEvent_whenReceivingFailedRequest() =
        runTest {
            createRecommendationClient(backgroundScope).register()
            failureCases.forEach { case ->
                val exception =
                    SdkException.FailedRequestException(
                        createHttpResponse(
                            case.status,
                            trackRequest
                        )
                    )
                val responseCaptor = slot<SdkEvent.Internal.Sdk.Answer.Response<*>>()
                everySuspend { mockRecommendationRequestFactory.create(searchEvent) } returns trackRequest
                everySuspend { mockNetworkClient.send(trackRequest) } returns Result.failure(
                    exception
                )
                everySuspend { mockSdkEventManager.emitEvent(capture(responseCaptor)) } returns Unit

                emitOnlineAndAwait(searchEvent) shouldBe 1

                val surfaced = responseCaptor.get().result.exceptionOrNull()
                assertSurfacedException(case, exception, surfaced)
                verifySuspend {
                    mockClientExceptionHandler.handleException(
                        exception,
                        "exception while consuming RecommendationTrackEvent events",
                        searchEvent
                    )
                }
                if (case.expectsErrorLog) {
                    verifySuspend {
                        mockSdkLogger.error("recommendation request failed with 404", exception)
                    }
                }
            }
        }

    @Test
    fun testHandleFailure_trackEvent_shouldEmitFailure_andHandleEvent_whenReceivingNonFailedRequestException() =
        runTest {
            createRecommendationClient(backgroundScope).register()
            val exception = SdkException.SdkDisabledException("SDK is disabled!")
            everySuspend { mockRecommendationRequestFactory.create(searchEvent) } returns trackRequest
            everySuspend { mockNetworkClient.send(trackRequest) } returns Result.failure(exception)

            emitOnlineAndAwait(searchEvent) shouldBe 1

            verifySuspend {
                mockSdkEventManager.emitEvent(
                    SdkEvent.Internal.Sdk.Answer.Response(
                        searchEvent.id,
                        Result.failure<Response>(exception)
                    )
                )
                mockClientExceptionHandler.handleException(
                    exception,
                    "exception while consuming RecommendationTrackEvent events",
                    searchEvent
                )
            }
        }

    @Test
    fun testHandleFailure_requestRecommendation_shouldSurfaceMappedException_andLogEvent_whenReceivingFailedRequestException() =
        runTest {
            createRecommendationClient(backgroundScope).register()
            failureCases.forEach { case ->
                val exception =
                    SdkException.FailedRequestException(
                        createHttpResponse(
                            case.status,
                            requestRecommendationUrlRequest
                        )
                    )
                val responseCaptor = slot<SdkEvent.Internal.Sdk.Answer.Response<*>>()
                everySuspend { mockRecommendationRequestFactory.create(requestRecommendationEvent) } returns requestRecommendationUrlRequest
                everySuspend { mockNetworkClient.send(requestRecommendationUrlRequest) } returns Result.failure(
                    exception
                )
                everySuspend { mockSdkEventManager.emitEvent(capture(responseCaptor)) } returns Unit

                emitInternalAndAwait(requestRecommendationEvent) shouldBe 1

                val surfaced = responseCaptor.get().result.exceptionOrNull()
                assertSurfacedException(case, exception, surfaced)
                verifySuspend {
                    mockClientExceptionHandler.handleException(
                        exception,
                        "exception while consuming RequestRecommendation events"
                    )
                }
                if (case.expectsErrorLog) {
                    verifySuspend {
                        mockSdkLogger.error("recommendation request failed with 404", exception)
                    }
                }
            }
        }

    @Test
    fun testHandleFailure_requestRecommendation_shouldEmitFailure_andLogEvent_whenReceivingNonFailedRequestException() =
        runTest {
            createRecommendationClient(backgroundScope).register()
            val exception = SdkException.SdkDisabledException("SDK disabled")
            everySuspend { mockRecommendationRequestFactory.create(requestRecommendationEvent) } returns requestRecommendationUrlRequest
            everySuspend { mockNetworkClient.send(requestRecommendationUrlRequest) } returns Result.failure(
                exception
            )

            emitInternalAndAwait(requestRecommendationEvent) shouldBe 1

            verifySuspend {
                mockSdkEventManager.emitEvent(
                    SdkEvent.Internal.Sdk.Answer.Response(
                        requestRecommendationEvent.id,
                        Result.failure<Response>(exception)
                    )
                )
                mockClientExceptionHandler.handleException(
                    exception,
                    "exception while consuming RequestRecommendation events"
                )
            }
        }

    @Test
    fun testHandleFailure_trackEvent_shouldEmitFailureResponse_andCallHandler_whenRequestFactoryThrows() =
        runTest {
            createRecommendationClient(backgroundScope).register()
            val exception = SdkException.SdkDisabledException("test exception")
            everySuspend { mockRecommendationRequestFactory.create(searchEvent) } throws exception

            emitOnlineAndAwait(searchEvent) shouldBe 1

            verifySuspend {
                mockSdkEventManager.emitEvent(
                    SdkEvent.Internal.Sdk.Answer.Response(
                        searchEvent.id,
                        Result.failure<Response>(exception)
                    )
                )
                mockClientExceptionHandler.handleException(
                    exception,
                    "exception while consuming RecommendationTrackEvent events",
                    searchEvent
                )
            }
            verifySuspend(VerifyMode.exactly(0)) {
                mockNetworkClient.send(any())
                mockEventsDao.removeEvent(searchEvent)
            }
        }

    @Test
    fun testHandleFailure_requestRecommendation_shouldEmitFailureResponse_andLog_whenRequestFactoryThrows() =
        runTest {
            createRecommendationClient(backgroundScope).register()
            val exception = SdkException.SdkDisabledException("test exception")
            everySuspend { mockRecommendationRequestFactory.create(requestRecommendationEvent) } throws exception

            emitInternalAndAwait(requestRecommendationEvent) shouldBe 1

            verifySuspend {
                mockSdkEventManager.emitEvent(
                    SdkEvent.Internal.Sdk.Answer.Response(
                        requestRecommendationEvent.id,
                        Result.failure<Response>(exception)
                    )
                )
                mockClientExceptionHandler.handleException(
                    exception,
                    "exception while consuming RequestRecommendation events"
                )
            }
            verifySuspend(VerifyMode.exactly(0)) {
                mockNetworkClient.send(any())
            }
        }

    private fun createHttpResponse(status: HttpStatusCode, request: UrlRequest) = Response(
        status = status,
        headers = Headers.Empty,
        originalRequest = request,
        bodyAsText = "{}"
    )

    private fun assertSurfacedException(
        case: FailureCase,
        original: SdkException.FailedRequestException,
        surfaced: Throwable?
    ) {
        val expected = case.surfacedException(original)
        if (case.shouldTransformException) {
            surfaced!!::class shouldBe expected::class
        } else {
            surfaced shouldBe original
        }
    }

    private suspend fun TestScope.emitOnlineAndAwait(event: OnlineSdkEvent): Int {
        val collected = backgroundScope.async { onlineSdkEventsFlow.take(1).toList() }
        onlineSdkEventsFlow.emit(event)
        advanceUntilIdle()
        return collected.await().size
    }

    private suspend fun TestScope.emitInternalAndAwait(event: SdkEvent): Int {
        val collected = backgroundScope.async { sdkEventsFlow.take(1).toList() }
        sdkEventsFlow.emit(event)
        advanceUntilIdle()
        return collected.await().size
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
            mockClientExceptionHandler,
            mockSdkLogger
        )
    }
}