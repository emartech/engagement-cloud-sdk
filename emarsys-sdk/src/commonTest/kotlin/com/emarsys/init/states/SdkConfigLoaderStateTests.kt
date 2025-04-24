package com.emarsys.init.states

import com.emarsys.EmarsysConfig
import com.emarsys.SdkConfig
import com.emarsys.enable.EnableOrganizerApi
import com.emarsys.enable.config.SdkConfigStoreApi
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
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

    private lateinit var mockSdkConfigLoader: SdkConfigStoreApi<SdkConfig>
    private lateinit var mockSetupOrganizer: EnableOrganizerApi

    private lateinit var sdkConfigLoaderState: SdkConfigLoaderState

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        mockSdkConfigLoader = mock()
        mockSetupOrganizer = mock(MockMode.autofill)

    }

    @Test
    fun testActive_should_loadSdkConfig_andDoNothing_whenNoConfigIsSaved() = runTest {
        sdkConfigLoaderState =
            SdkConfigLoaderState(
                mockSdkConfigLoader,
                mockSetupOrganizer,
                applicationScope = backgroundScope,
                sdkLogger = mock(MockMode.autofill)
            )
        everySuspend { mockSdkConfigLoader.load() } returns null

        sdkConfigLoaderState.active()
        advanceUntilIdle()

        verifySuspend { mockSdkConfigLoader.load() }
        verifySuspend(VerifyMode.exactly(0)) {
            mockSetupOrganizer.enable(any())
        }
    }

    @Test
    fun testActive_should_loadSdkConfig_andSetup_whenConfigIsSaved() = runTest {
        sdkConfigLoaderState =
            SdkConfigLoaderState(
                mockSdkConfigLoader, mockSetupOrganizer, applicationScope = TestScope(
                    StandardTestDispatcher()
                ), sdkLogger = mock(MockMode.autofill)
            )
        val testConfig = EmarsysConfig()
        everySuspend { mockSdkConfigLoader.load() } returns testConfig

        sdkConfigLoaderState.active()
        advanceUntilIdle()

        verifySuspend { mockSdkConfigLoader.load() }
        verifySuspend {
            mockSetupOrganizer.enable(testConfig)
        }
    }
}