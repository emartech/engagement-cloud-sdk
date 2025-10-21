package com.emarsys.api.setup

import com.emarsys.IosEmarsysConfig
import com.emarsys.api.config.IosConfig
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
        val iosConfig = IosEmarsysConfig("ABC-123")

        iosSetup.enableTracking(iosConfig)

        verifySuspend { mockSetup.enableTracking(iosConfig) }
    }

    @Test
    fun disableTracking_shouldDelegate_toTheSameMethod_onSetupApi() = runTest {

        iosSetup.disableTracking()

        verifySuspend { mockSetup.disableTracking() }
    }
}