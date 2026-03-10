package com.sap.ec.enable

import JsEngagementCloudSDKConfig
import com.sap.ec.context.SdkContextApi
import com.sap.ec.mobileengage.push.PushServiceApi
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlatformInitStateTests {
    private lateinit var platformInitState: PlatformInitState
    private lateinit var mockPushService: PushServiceApi
    private lateinit var mockSdkContext: SdkContextApi

    @BeforeTest
    fun setup() {
        val testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)

        mockSdkContext = mock(MockMode.autofill)
        every { mockSdkContext.sdkDispatcher } returns testDispatcher
        mockPushService = mock(MockMode.autofill)

        platformInitState = PlatformInitState(mockPushService, mockSdkContext)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun activate_shouldCallRegister_onJsBridge_ifSdkContext_hasConfig_andReturnSuccess() = runTest {
        val testConfig = JsEngagementCloudSDKConfig(applicationCode = "test-app-code")
        every { mockSdkContext.config } returns testConfig

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
            every { mockSdkContext.config } returns JsEngagementCloudSDKConfig(applicationCode = "test-app-code")
            val testException = Exception("failure")
            everySuspend { mockPushService.register(any()) } throws testException

            val result = platformInitState.active()

            result shouldBe Result.failure(testException)
            verifySuspend(VerifyMode.exactly(1)) {
                mockPushService.register(any())
            }
        }
}