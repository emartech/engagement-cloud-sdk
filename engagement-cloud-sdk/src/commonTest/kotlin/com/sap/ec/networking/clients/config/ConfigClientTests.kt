package com.sap.ec.networking.clients.config

import com.sap.ec.api.SdkState
import com.sap.ec.config.SdkConfig
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.channel.SdkEventManagerApi
import com.sap.ec.core.db.events.EventsDaoApi
import com.sap.ec.core.exceptions.SdkException.NetworkIOException
import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.clients.NetworkClientApi
import com.sap.ec.core.networking.model.Response
import com.sap.ec.core.networking.model.UrlRequest
import com.sap.ec.core.url.ECUrlType
import com.sap.ec.core.url.UrlFactoryApi
import com.sap.ec.event.OnlineSdkEvent
import com.sap.ec.event.SdkEvent
import com.sap.ec.mobileengage.config.FollowUpChangeAppCodeOrganizerApi
import com.sap.ec.networking.clients.contact.ContactTokenHandlerApi
import com.sap.ec.networking.clients.error.ClientExceptionHandler
import dev.mokkery.MockMode
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.resetAnswers
import dev.mokkery.resetCalls
import dev.mokkery.spy
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class ConfigClientTests {
    private companion object {
        val TEST_BASE_URL = Url("https://test-base-url/")
    }

    private lateinit var mockEcClient: NetworkClientApi
    private lateinit var mockUrlFactory: UrlFactoryApi
    private lateinit var mockContactTokenHandler: ContactTokenHandlerApi
    private lateinit var mockEventsDao: EventsDaoApi
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var mockSdkLogger: Logger
    private lateinit var mockConfig: SdkConfig
    private lateinit var mockClientExceptionHandler: ClientExceptionHandler
    private lateinit var onlineEvents: MutableSharedFlow<OnlineSdkEvent>
    private lateinit var mockSdkEventManager: SdkEventManagerApi
    private lateinit var mockFollowUpChangeAppCodeOrganizer: FollowUpChangeAppCodeOrganizerApi
    private lateinit var configClient: ConfigClient

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        mockEcClient = mock()
        mockUrlFactory = mock()
        mockContactTokenHandler = mock(MockMode.autofill)
        mockSdkContext = mock(MockMode.autofill)
        mockSdkLogger = mock(MockMode.autofill)
        mockConfig = mock()
        mockClientExceptionHandler = mock(MockMode.autofill)
        mockEventsDao = mock()
        onlineEvents = spy(MutableSharedFlow())
        mockSdkEventManager = mock()
        mockFollowUpChangeAppCodeOrganizer = mock(MockMode.autofill)
        every { mockSdkEventManager.onlineSdkEvents } returns onlineEvents

        everySuspend { mockContactTokenHandler.handleContactTokens(any()) } returns Unit
        everySuspend { mockSdkContext.config } returns mockConfig
        everySuspend { mockSdkContext.config = any() } returns Unit
        everySuspend { mockSdkLogger.error(any(), any<Throwable>()) } calls {
            (it.args[1] as Throwable).printStackTrace()
        }
    }

    @AfterTest
    fun tearDown() {
        resetCalls()
        resetAnswers()
    }

    private fun createConfigClient(applicationScope: CoroutineScope) =
        ConfigClient(
            mockEcClient,
            mockClientExceptionHandler,
            mockUrlFactory,
            mockSdkEventManager,
            mockSdkContext,
            mockContactTokenHandler,
            mockFollowUpChangeAppCodeOrganizer,
            mockEventsDao,
            mockSdkLogger,
            applicationScope,
        )

    @Test
    fun testConsumer_should_call_client_with_change_appcode_request_and_ack_event() = runTest {
        configClient = createConfigClient(backgroundScope)
        configClient.register()

        every { mockUrlFactory.create(ECUrlType.ChangeApplicationCode) }.returns(
            TEST_BASE_URL
        )
        val testResponse = createTestResponse("{}")
        everySuspend { mockEcClient.send(any()) }.returns(Result.success(testResponse))
        every { mockConfig.copyWith("NewAppCode") } returns mockConfig
        every { mockConfig.applicationCode } returns "testApplicationCode"
        val changeAppCode = SdkEvent.Internal.Sdk.ChangeAppCode(
            id = "changeApplicationCode",
            applicationCode = "NewAppCode"
        )

        val onlineSdkEvents = backgroundScope.async {
            onlineEvents.take(1).toList()
        }

        onlineEvents.emit(changeAppCode)

        advanceUntilIdle()

        onlineSdkEvents.await() shouldBe listOf(changeAppCode)

        verifySuspend(VerifyMode.order) {
            mockEcClient.send(any())
            mockSdkContext.setSdkState(SdkState.OnHold)
            mockContactTokenHandler.handleContactTokens(any())
            mockConfig.copyWith(applicationCode = "NewAppCode")
            mockSdkContext.config = mockConfig
            mockFollowUpChangeAppCodeOrganizer.organize()
            mockSdkContext.setSdkState(SdkState.Active)
            mockEventsDao.removeEvent(changeAppCode)
            mockSdkEventManager.emitEvent(
                SdkEvent.Internal.Sdk.Answer.Response(
                    originId = changeAppCode.id,
                    Result.success(testResponse)
                )
            )
        }
        verifySuspend(VerifyMode.exactly(0)) { mockSdkLogger.error(any(), any<Throwable>()) }
    }

    @Test
    fun testConsumer_should_reEmit_events_into_flow_when_there_is_a_network_error() = runTest {
        configClient = createConfigClient(backgroundScope)
        configClient.register()

        every { mockUrlFactory.create(ECUrlType.ChangeApplicationCode) } returns TEST_BASE_URL
        val testException = NetworkIOException("No Network")
        everySuspend { mockEcClient.send(any()) } returns Result.failure(testException)

        val changeAppCode = SdkEvent.Internal.Sdk.ChangeAppCode(
            id = "changeAppcode",
            applicationCode = "newAppCode"
        )
        everySuspend { mockSdkEventManager.emitEvent(changeAppCode) } returns Unit

        val onlineSdkEvents = backgroundScope.async {
            onlineEvents.take(1).toList()
        }

        onlineEvents.emit(changeAppCode)

        advanceUntilIdle()

        onlineSdkEvents.await() shouldBe listOf(changeAppCode)
        verifySuspend { mockEcClient.send(any()) }
        verifySuspend {
            mockSdkEventManager.emitEvent(
                changeAppCode
            )
        }
        verifySuspend {
            mockClientExceptionHandler.handleException(
                testException,
                any(),
                changeAppCode
            )
        }
        verifySuspend(VerifyMode.exactly(0)) { mockEventsDao.removeEvent(changeAppCode) }

    }

    @Test
    fun testConsumer_should_call_clientExceptionHandler_when_exception_happens() = runTest {
        configClient = createConfigClient(backgroundScope)
        configClient.register()

        val testException = Exception("Test Exception")

        every {
            mockUrlFactory.create(
                ECUrlType.ChangeApplicationCode
            )
        } throws testException
        val changeAppCode = SdkEvent.Internal.Sdk.ChangeAppCode(
            id = "changeAppCode",
            applicationCode = "newAppCode"
        )
        everySuspend { mockSdkEventManager.emitEvent(any()) } returns Unit

        val onlineSdkEvents = backgroundScope.async {
            onlineEvents.take(1).toList()
        }

        onlineEvents.emit(changeAppCode)

        advanceUntilIdle()

        onlineSdkEvents.await() shouldBe listOf(changeAppCode)
        verifySuspend(VerifyMode.exactly(0)) { mockEcClient.send(any()) }
        verifySuspend {
            mockClientExceptionHandler.handleException(
                testException,
                any(),
                changeAppCode
            )
        }
    }

    private fun createTestResponse(
        body: String = "{}",
        statusCode: HttpStatusCode = HttpStatusCode.OK
    ) = Response(
        UrlRequest(
            TEST_BASE_URL,
            HttpMethod.Post
        ), statusCode, Headers.Empty, bodyAsText = body
    )
}
