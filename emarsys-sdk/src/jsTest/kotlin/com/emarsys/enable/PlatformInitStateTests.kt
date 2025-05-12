package com.emarsys.enable

import JsEmarsysConfig
import com.emarsys.context.SdkContext
import com.emarsys.core.log.LogLevel
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.di.SdkKoinIsolationContext.koin
import com.emarsys.fake.FakeStringStorage
import com.emarsys.mobileengage.push.PushServiceApi
import com.emarsys.util.JsonUtil
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
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

class PlatformInitStateTests: KoinTest {

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
            StandardTestDispatcher(),
            StandardTestDispatcher(),
            mock(),
            LogLevel.Info,
            mutableSetOf(),
            logBreadcrumbsQueueSize = 10
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
    fun activate_shouldCallRegister_onJsBridge_ifSdkContext_hasConfig() = runTest {
        val testConfig = JsEmarsysConfig()
        testSdkContext.config = testConfig

        platformInitState.active()

        verifySuspend {
            mockPushService.register(testConfig)
            mockPushService.subscribeForPushMessages(testConfig)
        }
    }

    @Test
    fun activate_shouldNot_callRegister_onPushService_ifSdkContext_hasNoConfig() = runTest {
        platformInitState.active()

        verifySuspend(VerifyMode.exactly(0)) {
            mockPushService.register(any())
            mockPushService.subscribeForPushMessages(any())
        }
    }
}