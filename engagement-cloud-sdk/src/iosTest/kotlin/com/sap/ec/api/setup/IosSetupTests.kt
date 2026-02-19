package com.sap.ec.api.setup

import com.sap.ec.IosEngagementCloudSDKConfig
import dev.mokkery.MockMode
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class IosSetupTests {
    private lateinit var iosSetup: IosSetupApi
    private lateinit var mockSetup: SetupApi

    @BeforeTest
    fun setup() {
        mockSetup = mock(MockMode.autofill)
        iosSetup = IosSetup(mockSetup)
    }

    @Test
    fun enableTracking_shouldDelegate_toTheSameMethod_onSetupApi() = runTest {
        val iosConfig = IosEngagementCloudSDKConfig("ABC-123")

        iosSetup.enable(iosConfig)

        verifySuspend { mockSetup.enable(iosConfig) }
    }

    @Test
    fun disableTracking_shouldDelegate_toTheSameMethod_onSetupApi() = runTest {

        iosSetup.disable()

        verifySuspend { mockSetup.disable() }
    }
}