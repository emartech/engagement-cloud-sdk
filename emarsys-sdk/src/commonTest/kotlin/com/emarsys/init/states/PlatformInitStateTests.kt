package com.emarsys.init.states

import com.emarsys.setup.PlatformInitializerApi
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
    private lateinit var platformInitState: PlatformInitState

    @BeforeTest
    fun setup() {
        mockPlatformInitializer = mock()

        platformInitState = PlatformInitState(mockPlatformInitializer)
    }

    @Test
    fun testName() = runTest {
        platformInitState.name shouldBe "platformInitState"
    }

    @Test
    fun testActive_should_call_init_on_platformInitializer() = runTest {
        everySuspend { mockPlatformInitializer.init() } returns Unit
        platformInitState.active()
        verifySuspend { mockPlatformInitializer.init() }
    }
}