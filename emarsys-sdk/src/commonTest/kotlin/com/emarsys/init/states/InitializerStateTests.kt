package com.emarsys.init.states

import com.emarsys.core.log.Logger
import com.emarsys.enable.PlatformInitializerApi
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class InitializerStateTests {
    private lateinit var mockPlatformInitializer: PlatformInitializerApi
    private lateinit var mockLogger: Logger
    private lateinit var initializerState: InitializerState

    @BeforeTest
    fun setup() {
        mockPlatformInitializer = mock()
        mockLogger = mock(MockMode.autofill)
        initializerState = InitializerState(mockPlatformInitializer, mockLogger)
    }

    @Test
    fun active_shouldCallInit_onPlatformInitializer_andReturn_success() = runTest {
        everySuspend { mockPlatformInitializer.init() } returns Unit

        initializerState.active() shouldBe Result.success(Unit)
    }

    @Test
    fun active_shouldCallInit_onPlatformInitializer_andReturn_failure() = runTest {
        val testException = Exception("failure")
        everySuspend { mockPlatformInitializer.init() } throws testException

        initializerState.active() shouldBe Result.failure(testException)
    }
}