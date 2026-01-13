package com.emarsys.networking.clients.config

import com.emarsys.config.SdkConfig
import com.emarsys.context.SdkContextApi
import com.emarsys.core.channel.SdkEventManagerApi
import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.exceptions.SdkException.NetworkIOException
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.event.OnlineSdkEvent
import com.emarsys.event.SdkEvent
import com.emarsys.networking.clients.contact.ContactTokenHandlerApi
import com.emarsys.networking.clients.error.ClientExceptionHandler
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
import dev.mokkery.verify
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

    private lateinit var mockEmarsysClient: NetworkClientApi
    private lateinit var mockUrlFactory: UrlFactoryApi
    private lateinit var mockContactTokenHandler: ContactTokenHandlerApi
    private lateinit var mockEventsDao: EventsDaoApi
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var mockSdkLogger: Logger
    private lateinit var mockConfig: SdkConfig
    private lateinit var mockClientExceptionHandler: ClientExceptionHandler
    private lateinit var onlineEvents: MutableSharedFlow<OnlineSdkEvent>
    private lateinit var mockSdkEventManager: SdkEventManagerApi
    private lateinit var configClient: ConfigClient

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        mockEmarsysClient = mock()
        mockUrlFactory = mock()
        mockContactTokenHandler = mock()
        mockSdkContext = mock()
        mockSdkLogger = mock(MockMode.autofill)
        mockConfig = mock()
        mockClientExceptionHandler = mock(MockMode.autofill)
        mockEventsDao = mock()
        onlineEvents = spy(MutableSharedFlow())
        mockSdkEventManager = mock()
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
            mockEmarsysClient,
            mockClientExceptionHandler,
            mockUrlFactory,
            mockSdkEventManager,
            mockSdkContext,
            mockContactTokenHandler,
            mockEventsDao,
            mockSdkLogger,
            applicationScope,
        )

    @Test
    fun testConsumer_should_call_client_with_change_appcode_request_and_ack_event() = runTest {
        configClient = createConfigClient(backgroundScope)
        configClient.register()

        every { mockUrlFactory.create(EmarsysUrlType.ChangeApplicationCode) }.returns(
            TEST_BASE_URL
        )
        everySuspend { mockEmarsysClient.send(any()) }.returns(Result.success(createTestResponse("{}")))
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
        verifySuspend { mockEmarsysClient.send(any()) }
        verify { mockConfig.copyWith(applicationCode = "NewAppCode") }
        verify { mockSdkContext.config = mockConfig }
        verifySuspend(VerifyMode.exactly(0)) { mockSdkLogger.error(any(), any<Throwable>()) }
        verifySuspend { mockEventsDao.removeEvent(changeAppCode) }
    }

    @Test
    fun testConsumer_should_reEmit_events_into_flow_when_there_is_a_network_error() = runTest {
        configClient = createConfigClient(backgroundScope)
        configClient.register()

        every { mockUrlFactory.create(EmarsysUrlType.ChangeApplicationCode) } returns TEST_BASE_URL
        val testException = NetworkIOException("No Network")
        everySuspend { mockEmarsysClient.send(any()) } returns Result.failure(testException)

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
        verifySuspend { mockEmarsysClient.send(any()) }
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
                EmarsysUrlType.ChangeApplicationCode
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
        verifySuspend(VerifyMode.exactly(0)) { mockEmarsysClient.send(any()) }
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
