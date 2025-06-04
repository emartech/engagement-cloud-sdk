package com.emarsys.networking.clients.config

import com.emarsys.SdkConfig
import com.emarsys.context.SdkContextApi
import com.emarsys.core.channel.SdkEventManagerApi
import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.exceptions.FailedRequestException
import com.emarsys.core.exceptions.MissingApplicationCodeException
import com.emarsys.core.exceptions.RetryLimitReachedException
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.context.RequestContextApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.networking.clients.contact.ContactTokenHandlerApi
import com.emarsys.networking.clients.event.model.OnlineSdkEvent
import com.emarsys.networking.clients.event.model.SdkEvent
import com.emarsys.util.JsonUtil
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
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
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
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
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
    private lateinit var mockRequestContext: RequestContextApi
    private lateinit var json: Json
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
        mockRequestContext = mock(MockMode.autofill)
        every { mockRequestContext.refreshToken } returns "testRefreshToken"
        json = JsonUtil.json
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
            mockUrlFactory,
            mockSdkEventManager,
            mockRequestContext,
            mockSdkContext,
            mockContactTokenHandler,
            mockEventsDao,
            json,
            mockSdkLogger,
            applicationScope,
        )

    @Test
    fun testConsumer_should_call_client_with_change_appcode_request_and_ack_event() = runTest {
        configClient = createConfigClient(backgroundScope)
        configClient.register()

        every { mockUrlFactory.create(EmarsysUrlType.CHANGE_APPLICATION_CODE) }.returns(
            TEST_BASE_URL
        )
        everySuspend { mockEmarsysClient.send(any(), any()) }.returns(createTestResponse("{}"))
        every { mockConfig.copyWith("NewAppCode", null, null) } returns mockConfig
        every { mockConfig.merchantId } returns null
        every { mockConfig.applicationCode } returns "testApplicationCode"
        every { mockConfig.sharedSecret } returns null
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
        verifySuspend { mockEmarsysClient.send(any(), any()) }
        verify { mockConfig.copyWith(applicationCode = "NewAppCode", null, null) }
        verify { mockSdkContext.config = mockConfig }
        verifySuspend(VerifyMode.exactly(0)) { mockSdkLogger.error(any(), any<Throwable>()) }
        verifySuspend { mockEventsDao.removeEvent(changeAppCode) }
    }

    @Test
    fun testConsumer_should_call_client_with_change_merchantId_request_and_ack_event() = runTest {
        configClient = createConfigClient(backgroundScope)
        configClient.register()

        every { mockUrlFactory.create(EmarsysUrlType.REFRESH_TOKEN) }.returns(TEST_BASE_URL)
        everySuspend { mockEmarsysClient.send(any(), any()) }.returns(createTestResponse("{}"))
        every { mockConfig.copyWith(null, "newMerchantId", null) } returns mockConfig
        every { mockConfig.merchantId } returns "testMerchantId"
        every { mockConfig.applicationCode } returns null
        every { mockConfig.sharedSecret } returns null
        val changeMerchantId = SdkEvent.Internal.Sdk.ChangeMerchantId(
            id = "changeMerchantId",
            merchantId = "newMerchantId"
        )

        val onlineSdkEvents = backgroundScope.async {
            onlineEvents.take(1).toList()
        }

        onlineEvents.emit(changeMerchantId)

        advanceUntilIdle()

        onlineSdkEvents.await() shouldBe listOf(changeMerchantId)
        verifySuspend { mockEmarsysClient.send(any(), any()) }
        verify {
            mockConfig.copyWith(
                merchantId = "newMerchantId",
                applicationCode = null,
                sharedSecret = null
            )
        }
        verify { mockSdkContext.config = mockConfig }
        verifySuspend(VerifyMode.exactly(0)) { mockSdkLogger.error(any(), any<Throwable>()) }
        verifySuspend { mockEventsDao.removeEvent(changeMerchantId) }
    }

    @Test
    fun testConsumer_should_reEmit_events_into_flow_when_there_is_a_network_error() = runTest {
        configClient = createConfigClient(backgroundScope)
        configClient.register()

        every { mockUrlFactory.create(EmarsysUrlType.REFRESH_TOKEN) } returns TEST_BASE_URL
        everySuspend { mockEmarsysClient.send(any(), any()) } calls { args ->
            (args.arg(1) as suspend () -> Unit).invoke()
            throw IOException("No Internet")
        }

        val changeMerchantId = SdkEvent.Internal.Sdk.ChangeMerchantId(
            id = "changeMerchantId",
            merchantId = "newMerchantId"
        )
        everySuspend { mockSdkEventManager.emitEvent(changeMerchantId) } returns Unit

        val onlineSdkEvents = backgroundScope.async {
            onlineEvents.take(1).toList()
        }

        onlineEvents.emit(changeMerchantId)

        advanceUntilIdle()

        onlineSdkEvents.await() shouldBe listOf(changeMerchantId)
        verifySuspend { mockEmarsysClient.send(any(), any()) }
        verifySuspend { mockSdkLogger.error(any(), any<Throwable>()) }
        verifySuspend { mockSdkEventManager.emitEvent(changeMerchantId) }
        verifySuspend(VerifyMode.exactly(0)) { mockEventsDao.removeEvent(changeMerchantId) }

    }

    @Test
    fun testConsumer_should_not_ack_event_when_unknown_exception_happens() = runTest {
        configClient = createConfigClient(backgroundScope)
        configClient.register()

        every {
            mockUrlFactory.create(
                EmarsysUrlType.REFRESH_TOKEN
            )
        } throws RuntimeException("exception")
        val changeMerchantId = SdkEvent.Internal.Sdk.ChangeMerchantId(
            id = "changeMerchantId",
            merchantId = "newMerchantId"
        )

        val onlineSdkEvents = backgroundScope.async {
            onlineEvents.take(1).toList()
        }

        onlineEvents.emit(changeMerchantId)

        advanceUntilIdle()

        onlineSdkEvents.await() shouldBe listOf(changeMerchantId)
        verifySuspend(VerifyMode.exactly(0)) { mockEmarsysClient.send(any(), any()) }
        verifySuspend { mockSdkLogger.error(any(), any<Throwable>()) }
        verifySuspend(VerifyMode.exactly(0)) { mockEventsDao.removeEvent(changeMerchantId) }

    }

    @Test
    fun testConsumer_should_ack_event_when_known_exception_happens() = forAll(
        table(
            headers("exception"),
            listOf(
                row(
                    FailedRequestException(
                        createTestResponse(
                            statusCode = HttpStatusCode.BadRequest
                        )
                    )
                ),
                row(RetryLimitReachedException("Retry limit reached")),
                row(
                    MissingApplicationCodeException("Missing app code")
                ),
            )
        )
    ) { testException ->
        runTest {
            configClient = createConfigClient(backgroundScope)
            configClient.register()

            every {
                mockUrlFactory.create(
                    EmarsysUrlType.REFRESH_TOKEN
                )
            } throws testException
            val changeMerchantId = SdkEvent.Internal.Sdk.ChangeMerchantId(
                id = "changeMerchantId",
                merchantId = "newMerchantId"
            )

            val onlineSdkEvents = backgroundScope.async {
                onlineEvents.take(1).toList()
            }

            onlineEvents.emit(changeMerchantId)

            advanceUntilIdle()

            onlineSdkEvents.await() shouldBe listOf(changeMerchantId)
            verifySuspend(VerifyMode.exactly(0)) { mockEmarsysClient.send(any(), any()) }
            verifySuspend(VerifyMode.exactly(0)) { mockSdkLogger.error(any(), any<Throwable>()) }
            verifySuspend { mockEventsDao.removeEvent(changeMerchantId) }
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
