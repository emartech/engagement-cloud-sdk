package com.emarsys.init.states

import com.emarsys.core.Registerable
import dev.mokkery.MockMode
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class RegisterSdkEventDistributorStateTests {
    private lateinit var mockSdkEventDistributor: Registerable
    private lateinit var registerSdkEventDistributorState: RegisterSdkEventDistributorState

    @BeforeTest
    fun setUp() {
        mockSdkEventDistributor = mock(MockMode.autoUnit)
        registerSdkEventDistributorState = RegisterSdkEventDistributorState(mockSdkEventDistributor)
    }

    @Test
    fun testActive_should_registerAllConsumers() = runTest {
        val result = registerSdkEventDistributorState.active()

        verifySuspend { mockSdkEventDistributor.register() }
        result shouldBe Result.success(Unit)
    }
}