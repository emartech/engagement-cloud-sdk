package com.sap.ec.init.states

import com.sap.ec.core.log.SdkLogger
import com.sap.ec.enable.PlatformInitializerApi
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class PlatformInitStateTests {
    private lateinit var mockPlatformInitializer: PlatformInitializerApi
    private lateinit var initializerState: InitializerState

    @BeforeTest
    fun setup() {
        mockPlatformInitializer = mock()

        initializerState = InitializerState(mockPlatformInitializer, SdkLogger("TestLoggerName", mock(MockMode.autofill), sdkContext = mock()))
    }

    @Test
    fun testName() = runTest {
        initializerState.name shouldBe "platformInitState"
    }

    @Test
    fun testActive_should_call_init_on_platformInitializer() = runTest {
        everySuspend { mockPlatformInitializer.init() } returns Unit
        initializerState.active()
        verifySuspend { mockPlatformInitializer.init() }
    }
}