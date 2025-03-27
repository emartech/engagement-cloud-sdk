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
import com.emarsys.di.DispatcherTypes
import com.emarsys.di.EventFlowTypes
import com.emarsys.di.NetworkClientTypes
import com.emarsys.di.SdkKoinIsolationContext.koin
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
import dev.mokkery.resetAnswers
import dev.mokkery.resetCalls
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
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.koin.core.Koin
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.test.KoinTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConfigClientTests : KoinTest {
    override fun getKoin(): Koin = koin

    private companion object {
        val TEST_BASE_URL = Url("https://test-base-url/")
        val mockEmarsysClient: NetworkClientApi = mock()
        val mockUrlFactory: UrlFactoryApi = mock()
        val mockContactTokenHandler: ContactTokenHandlerApi = mock()
        val mockSdkContext: SdkContextApi = mock()
        val mockSdkLogger: Logger = mock(MockMode.autofill)
        val mockConfig: SdkConfig = mock()
        val sessionContext = SessionContext(refreshToken = "testRefreshToken")
        val json = JsonUtil.json
        val sdkEventFlow: MutableSharedFlow<SdkEvent> = spy(MutableSharedFlow(replay = 5))
    }

    init {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    private lateinit var sdkDispatcher: CoroutineDispatcher
    private lateinit var testModules: Module

    private fun createConfigClientTestModules() {
        testModules = module {
            single<NetworkClientApi>(named(NetworkClientTypes.Emarsys)) { mockEmarsysClient }
            single<UrlFactoryApi> { mockUrlFactory }
            single<ContactTokenHandlerApi> { mockContactTokenHandler }
            single<SdkContextApi> { mockSdkContext }
            single<Logger> { mockSdkLogger }
            single<SdkConfig> { mockConfig }
            single<Json> { json }
            single<SessionContext> { sessionContext }
            single<CoroutineDispatcher>(named(DispatcherTypes.Sdk)) { sdkDispatcher }
            single<MutableSharedFlow<SdkEvent>>(named(EventFlowTypes.InternalEventFlow)) { sdkEventFlow }
        }
        koin.loadModules(listOf(testModules))
    }

    @BeforeTest
    fun setup() = runTest {
        sdkDispatcher = StandardTestDispatcher()
        createConfigClientTestModules()

        everySuspend { mockContactTokenHandler.handleContactTokens(any()) } returns Unit
        everySuspend { mockSdkContext.config } returns mockConfig
        everySuspend { mockSdkContext.config = any() } returns Unit
        everySuspend { mockSdkLogger.error(any(), any<Throwable>()) } calls {
            (it.args[1] as Throwable).printStackTrace()
            throw it.args[1] as Throwable
        }

        ConfigClient
    }

    @AfterTest
    fun tearDown() {
        resetCalls()
        resetAnswers()
        koin.unloadModules(listOf(testModules))
    }

    @Test
    fun testConsumer_should_call_client_with_change_appcode_request() = runTest {
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