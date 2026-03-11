package com.sap.ec.init.states

import com.sap.ec.TestEngagementCloudSDKConfig
import com.sap.ec.context.SdkContextApi
import com.sap.ec.enable.EnableOrganizerApi
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SdkConfigLoaderStateTests {

    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var mockSetupOrganizer: EnableOrganizerApi

    private lateinit var sdkConfigLoaderState: SdkConfigLoaderState

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        mockSdkContext = mock(MockMode.autofill)
        mockSetupOrganizer = mock(MockMode.autofill)
    }

    @Test
    fun testActive_should_loadSdkConfig_andDoNothing_whenNoConfigIsSaved() = runTest {
        sdkConfigLoaderState =
            SdkConfigLoaderState(
                mockSdkContext,
                mockSetupOrganizer,
                applicationScope = backgroundScope,
                sdkLogger = mock(MockMode.autofill)
            )
        everySuspend { mockSdkContext.getSdkConfig() } returns null

        val result = sdkConfigLoaderState.active()
        advanceUntilIdle()

        result shouldBe Result.success(Unit)
        verifySuspend { mockSdkContext.getSdkConfig() }
        verifySuspend(VerifyMode.exactly(0)) {
            mockSetupOrganizer.enable(any())
        }
    }

    @Test
    fun testActive_should_loadSdkConfig_andSetup_whenConfigIsSaved() = runTest {
        sdkConfigLoaderState =
            SdkConfigLoaderState(
                mockSdkContext, mockSetupOrganizer, applicationScope = TestScope(
                    StandardTestDispatcher()
                ), sdkLogger = mock(MockMode.autofill)
            )
        val testConfig = TestEngagementCloudSDKConfig(applicationCode = "test-app-code")
        everySuspend { mockSdkContext.getSdkConfig() } returns testConfig

        val result = sdkConfigLoaderState.active()
        advanceUntilIdle()

        result shouldBe Result.success(Unit)
        verifySuspend { mockSdkContext.getSdkConfig() }
        verifySuspend { mockSetupOrganizer.enable(testConfig) }
    }

    @Test
    fun testActive_should_returnFailure_whenEnableThrowsException() = runTest {
        sdkConfigLoaderState =
            SdkConfigLoaderState(
                mockSdkContext,
                mockSetupOrganizer,
                applicationScope = backgroundScope,
                sdkLogger = mock(MockMode.autofill)
            )
        val testConfig = TestEngagementCloudSDKConfig(applicationCode = "test-app-code")
        val testException = RuntimeException("Enable failed")
        everySuspend { mockSdkContext.getSdkConfig() } returns testConfig
        everySuspend { mockSetupOrganizer.enable(any()) } throws testException

        val result = sdkConfigLoaderState.active()
        advanceUntilIdle()

        result.isSuccess shouldBe true
        verifySuspend { mockSdkContext.getSdkConfig() }
        verifySuspend { mockSetupOrganizer.enable(testConfig) }
    }
}