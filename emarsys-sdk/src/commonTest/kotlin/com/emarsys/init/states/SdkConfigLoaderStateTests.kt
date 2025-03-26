package com.emarsys.init.states

import com.emarsys.EmarsysConfig
import com.emarsys.SdkConfig
import com.emarsys.setup.SetupOrganizerApi
import com.emarsys.setup.config.SdkConfigStoreApi
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class SdkConfigLoaderStateTests {

    private lateinit var mockSdkConfigLoader: SdkConfigStoreApi<SdkConfig>
    private lateinit var mockSetupOrganizer: SetupOrganizerApi

    private lateinit var sdkConfigLoaderState: SdkConfigLoaderState

    @BeforeTest
    fun setup() {
        mockSdkConfigLoader = mock()
        mockSetupOrganizer = mock(MockMode.autofill)
        sdkConfigLoaderState =
            SdkConfigLoaderState(mockSdkConfigLoader, mockSetupOrganizer, sdkLogger = mock(MockMode.autofill))
    }

    @Test
    fun testActive_should_loadSdkConfig_andDoNothing_whenNoConfigIsSaved() = runTest {
        everySuspend { mockSdkConfigLoader.load() } returns null

        sdkConfigLoaderState.active()

        verifySuspend { mockSdkConfigLoader.load() }
        verifySuspend(VerifyMode.exactly(0)) {
            mockSetupOrganizer.setup(any())
        }
    }

    @Test
    fun testActive_should_loadSdkConfig_andSetup_whenConfigIsSaved() = runTest {
        val testConfig = EmarsysConfig()
        everySuspend { mockSdkConfigLoader.load() } returns testConfig

        sdkConfigLoaderState.active()

        verifySuspend { mockSdkConfigLoader.load() }
        verifySuspend {
            mockSetupOrganizer.setup(testConfig)
        }
    }
}