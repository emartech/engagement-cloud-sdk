package com.emarsys.networking.clients.config

import com.emarsys.SdkConfig
import com.emarsys.context.SdkContextApi
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.session.SessionContext
import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.networking.clients.contact.ContactTokenHandlerApi
import com.emarsys.networking.clients.event.model.SdkEvent
import com.emarsys.util.JsonUtil
import dev.mokkery.MockMode
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.spy
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
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

    init {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    private lateinit var mockEmarsysClient: NetworkClientApi
    private lateinit var mockUrlFactory: UrlFactoryApi
    private lateinit var sdkEventFlow: MutableSharedFlow<SdkEvent>
    private lateinit var sdkDispatcher: CoroutineDispatcher
    private lateinit var sessionContext: SessionContext
    private lateinit var mockContactTokenHandler: ContactTokenHandlerApi
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var json: Json
    private lateinit var mockSdkLogger: Logger
    private lateinit var mockConfig: SdkConfig


    @BeforeTest
    fun setup() = runTest {
        mockSdkLogger = mock(MockMode.autofill)
        json = JsonUtil.json
        sdkEventFlow = spy(MutableSharedFlow(replay = 5))
        mockEmarsysClient = mock()
        mockUrlFactory = mock()
        mockContactTokenHandler = mock()
        sessionContext = SessionContext(refreshToken = "testRefreshToken")
        mockSdkContext = mock()
        Dispatchers.setMain(StandardTestDispatcher())
        sdkDispatcher =
            StandardTestDispatcher()


        everySuspend { mockContactTokenHandler.handleContactTokens(any()) } returns Unit
        mockConfig = mock()
        everySuspend { mockSdkContext.config } returns mockConfig
        everySuspend { mockSdkContext.config = any() } returns Unit
        everySuspend { mockSdkLogger.error(any(), any<Throwable>()) } calls {
            (it.args[1] as Throwable).printStackTrace()
            throw it.args[1] as Throwable
        }
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInit_should_start_consume_on_channel() = runTest {
        val configClient = ConfigClient(
            emarsysNetworkClient = mockEmarsysClient,
            urlFactory = mockUrlFactory,
            sdkEventFlow = sdkEventFlow,
            json = json,
            sdkLogger = mockSdkLogger,
            sessionContext = sessionContext,
            sdkDispatcher = sdkDispatcher,
            contactTokenHandler = mockContactTokenHandler,
            sdkContext = mockSdkContext
        )

        advanceUntilIdle()

        verifySuspend { sdkEventFlow.filter(any()) }
        verifySuspend(VerifyMode.exactly(0)) { mockSdkLogger.error(any(), any<Throwable>()) }

    }

    @Test
    fun testConsumer_should_call_client_with_change_appcode_request() = runTest {
        val configClient = ConfigClient(
            emarsysNetworkClient = mockEmarsysClient,
            urlFactory = mockUrlFactory,
            sdkEventFlow = sdkEventFlow,
            json = json,
            sdkLogger = mockSdkLogger,
            sessionContext = sessionContext,
            sdkDispatcher = sdkDispatcher,
            contactTokenHandler = mockContactTokenHandler,
            sdkContext = mockSdkContext
        )
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

        sdkEventFlow.emit(changeAppCode)

        advanceUntilIdle()

        verifySuspend { mockEmarsysClient.send(any()) }
        verify { mockConfig.copyWith(applicationCode = "NewAppCode", null, null) }
        verify { mockSdkContext.config = mockConfig }
        verifySuspend(VerifyMode.exactly(0)) { mockSdkLogger.error(any(), any<Throwable>()) }

    }

    @Test
    fun testConsumer_should_call_client_with_change_merchantId_request() = runTest {
        val configClient = ConfigClient(
            emarsysNetworkClient = mockEmarsysClient,
            urlFactory = mockUrlFactory,
            sdkEventFlow = sdkEventFlow,
            json = json,
            sdkLogger = mockSdkLogger,
            sessionContext = sessionContext,
            sdkDispatcher = sdkDispatcher,
            contactTokenHandler = mockContactTokenHandler,
            sdkContext = mockSdkContext
        )
        every { mockUrlFactory.create(EmarsysUrlType.REFRESH_TOKEN, null) }.returns(TEST_BASE_URL)

        everySuspend { mockEmarsysClient.send(any()) }.returns(createTestResponse("{}"))
        every { mockConfig.copyWith(null, "newMerchantId", null) } returns mockConfig
        every { mockConfig.merchantId } returns "testMerchantId"
        every { mockConfig.applicationCode } returns null
        every { mockConfig.sharedSecret } returns null
        val changeMerchantId = SdkEvent.Internal.Sdk.ChangeMerchantId(
            "changeMerchantId",
            buildJsonObject { put("merchantId", JsonPrimitive("newMerchantId")) })

        sdkEventFlow.emit(changeMerchantId)

        advanceUntilIdle()

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