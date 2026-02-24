package com.sap.ec.enable

import JsEngagementCloudSDKConfig
import com.sap.ec.context.DefaultUrls
import com.sap.ec.context.SdkContext
import com.sap.ec.core.log.LogLevel
import com.sap.ec.core.storage.StringStorageApi
import com.sap.ec.di.SdkKoinIsolationContext.koin
import com.sap.ec.fake.FakeStringStorage
import com.sap.ec.mobileengage.push.PushServiceApi
import com.sap.ec.util.JsonUtil
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.koin.core.Koin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.test.KoinTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class PlatformInitStateTests : KoinTest {

    override fun getKoin(): Koin = koin

    private lateinit var testModule: Module

    private lateinit var platformInitState: PlatformInitState
    private lateinit var mockPushService: PushServiceApi
    private lateinit var testSdkContext: SdkContext

    @BeforeTest
    fun setup() = runTest {
        testModule = module {
            single<StringStorageApi> { FakeStringStorage() }
            single<Json> { JsonUtil.json }
        }
        koin.loadModules(listOf(testModule))

        testSdkContext = SdkContext(
            sdkDispatcher = StandardTestDispatcher(),
            mainDispatcher = StandardTestDispatcher(),
            defaultUrls = DefaultUrls("", "", "", "", "", "", ""),
            remoteLogLevel = LogLevel.Error,
            features = mutableSetOf(),
            logBreadcrumbsQueueSize = 10,
            onContactLinkingFailed = null
        )
        mockPushService = mock()
        everySuspend { mockPushService.register(any()) } returns Unit
        everySuspend { mockPushService.subscribeForPushMessages(any()) } returns Unit
        platformInitState = PlatformInitState(mockPushService, testSdkContext)
    }

    @AfterTest
    fun tearDown() {
        koin.unloadModules(listOf(testModule))
    }

    @Test
    fun activate_shouldCallRegister_onJsBridge_ifSdkContext_hasConfig_andReturnSuccess() = runTest {
        val testConfig = JsEngagementCloudSDKConfig(applicationCode = "test-app-code")
        testSdkContext.config = testConfig

        val result = platformInitState.active()

        result shouldBe Result.success(Unit)
        verifySuspend {
            mockPushService.register(testConfig)
            mockPushService.subscribeForPushMessages(testConfig)
        }
    }

    @Test
    fun activate_shouldNot_callRegister_onPushService_ifSdkContext_hasNoConfig_andReturnSuccess() =
        runTest {
            val result = platformInitState.active()

            result shouldBe Result.success(Unit)
            verifySuspend(VerifyMode.exactly(0)) {
                mockPushService.register(any())
                mockPushService.subscribeForPushMessages(any())
            }
        }

    @Test
    fun activate_should_callRegister_onPushService_ifSdkContext_hasConfig_andReturnFailure_ifErrorHappens() =
        runTest {
            testSdkContext.config = JsEngagementCloudSDKConfig(applicationCode = "test-app-code")
            val testException = Exception("failure")
            everySuspend { mockPushService.register(any()) } throws testException

            val result = platformInitState.active()

            result shouldBe Result.failure(testException)
            verifySuspend(VerifyMode.exactly(1)) {
                mockPushService.register(any())
            }
        }
}