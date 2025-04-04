package com.emarsys.networking.clients.config

import com.emarsys.SdkConfig
import com.emarsys.context.SdkContextApi
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.session.SessionContext
import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.networking.clients.contact.ContactTokenHandlerApi
import com.emarsys.networking.clients.event.model.OnlineSdkEvent
import com.emarsys.networking.clients.event.model.SdkEvent
import com.emarsys.util.JsonUtil
import dev.mokkery.MockMode
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
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
import kotlinx.coroutines.CoroutineDispatcher
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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConfigClientTests {
    private companion object {
        val TEST_BASE_URL = Url("https://test-base-url/")
    }

    private lateinit var sdkDispatcher: CoroutineDispatcher
    private lateinit var mockEmarsysClient: NetworkClientApi
    private lateinit var mockUrlFactory: UrlFactoryApi
    private lateinit var mockContactTokenHandler: ContactTokenHandlerApi
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var mockSdkLogger: Logger
    private lateinit var mockConfig: SdkConfig
    private lateinit var sessionContext: SessionContext
    private lateinit var json: Json
    private lateinit var onlineEvents: MutableSharedFlow<OnlineSdkEvent>
    private lateinit var sdkEventDistributor: SdkEventDistributorApi
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
        sessionContext = SessionContext(refreshToken = "testRefreshToken")
        json = JsonUtil.json
        onlineEvents = spy(MutableSharedFlow(replay = 5))
        sdkEventDistributor = mock()
        every { sdkEventDistributor.onlineSdkEvents } returns onlineEvents
        sdkDispatcher = StandardTestDispatcher()

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
            sdkEventDistributor,
            sessionContext,
            mockSdkContext,
            mockContactTokenHandler,
            json,
            mockSdkLogger,
            applicationScope
        )

    @Test
    fun testConsumer_should_call_client_with_change_appcode_request() = runTest {
        configClient = createConfigClient(backgroundScope)
        configClient.register()

        every { mockUrlFactory.create(EmarsysUrlType.CHANGE_APPLICATION_CODE, null) }.returns(
            TEST_BASE_URL
        )
        everySuspend { mockEmarsysClient.send(any()) }.returns(createTestResponse("{}"))
        every { mockConfig.copyWith("NewAppCode", null, null) } returns mockConfig
        every { mockConfig.merchantId } returns null
        every { mockConfig.applicationCode } returns "testApplicationCode"
        every { mockConfig.sharedSecret } returns null
        val changeAppCode = SdkEvent.Internal.Sdk.ChangeAppCode(
            "changeApplicationCode",
            buildJsonObject { put("applicationCode", JsonPrimitive("NewAppCode")) })

        val onlineSdkEvents = backgroundScope.async {
            onlineEvents.take(1).toList()
        }

        onlineEvents.emit(changeAppCode)

        advanceUntilIdle()

        onlineSdkEvents.await() shouldBe listOf(changeAppCode)
        verifySuspend { mockEmarsysClient.send(any()) }
        verify { mockConfig.copyWith(applicationCode = "NewAppCode", null, null) }
        verify { mockSdkContext.config = mockConfig }
        verifySuspend(VerifyMode.exactly(0)) { mockSdkLogger.error(any(), any<Throwable>()) }
    }

    @Test
    fun testConsumer_should_call_client_with_change_merchantId_request() = runTest {
        configClient = createConfigClient(backgroundScope)
        configClient.register()

        every { mockUrlFactory.create(EmarsysUrlType.REFRESH_TOKEN, null) }.returns(TEST_BASE_URL)
        everySuspend { mockEmarsysClient.send(any()) }.returns(createTestResponse("{}"))
        every { mockConfig.copyWith(null, "newMerchantId", null) } returns mockConfig
        every { mockConfig.merchantId } returns "testMerchantId"
        every { mockConfig.applicationCode } returns null
        every { mockConfig.sharedSecret } returns null
        val changeMerchantId = SdkEvent.Internal.Sdk.ChangeMerchantId(
            "changeMerchantId",
            buildJsonObject { put("merchantId", JsonPrimitive("newMerchantId")) })

        val onlineSdkEvents = backgroundScope.async {
            onlineEvents.take(1).toList()
        }

        onlineEvents.emit(changeMerchantId)

        advanceUntilIdle()

        onlineSdkEvents.await() shouldBe listOf(changeMerchantId)
        verifySuspend { mockEmarsysClient.send(any()) }
        verify {
            mockConfig.copyWith(
                merchantId = "newMerchantId",
                applicationCode = null,
                sharedSecret = null
            )
        }
        verify { mockSdkContext.config = mockConfig }
        verifySuspend(VerifyMode.exactly(0)) { mockSdkLogger.error(any(), any<Throwable>()) }
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
