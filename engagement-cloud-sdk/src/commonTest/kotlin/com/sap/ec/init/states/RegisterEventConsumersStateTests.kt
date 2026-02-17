package com.sap.ec.init.states

import com.sap.ec.core.Registerable
import dev.mokkery.MockMode
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class RegisterEventConsumersStateTests {
    private lateinit var mockInAppEventConsumer: Registerable
    private lateinit var consumers: List<Registerable>
    private lateinit var registerEventConsumersState: RegisterEventConsumersState

    @BeforeTest
    fun setUp() {
        mockInAppEventConsumer = mock(MockMode.autoUnit)
        consumers = listOf(mockInAppEventConsumer)
        registerEventConsumersState = RegisterEventConsumersState(consumers)
    }

    @Test
    fun testActive_should_registerAllConsumers() = runTest {
        val result = registerEventConsumersState.active()

        verifySuspend {
            mockInAppEventConsumer.register()
        }
        result shouldBe Result.success(Unit)
    }
}