package com.sap.ec.init.states

import com.sap.ec.core.Registerable
import com.sap.ec.core.log.SdkLogger
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class RegisterWatchdogsStateTests {
    private lateinit var registerWatchDogsState: RegisterWatchdogsState
    private lateinit var lifecycleWatchDog: Registerable
    private lateinit var connectionWatchDog: Registerable

    @BeforeTest
    fun setup() {
        lifecycleWatchDog = mock()
        connectionWatchDog = mock()
        registerWatchDogsState = RegisterWatchdogsState(
            lifecycleWatchDog,
            connectionWatchDog,
            SdkLogger("TestLoggerName", mock(MockMode.autofill), sdkContext = mock())
        )
    }

    @Test
    fun testName() = runTest {
        registerWatchDogsState.name shouldBe "registerWatchdogsState"
    }

    @Test
    fun testActive_should_call_register_on_watchdogs_and_returnSuccessResult() = runTest {
        everySuspend { lifecycleWatchDog.register() } returns Unit
        everySuspend { connectionWatchDog.register() } returns Unit

        registerWatchDogsState.active() shouldBe Result.success(Unit)

        verifySuspend { lifecycleWatchDog.register() }
        verifySuspend { connectionWatchDog.register() }
    }

    @Test
    fun testActive_should_return_failureResult_ifRegistrationFails() = runTest {
        val testException = Exception("failure")
        everySuspend { lifecycleWatchDog.register() }  throws testException
        everySuspend { connectionWatchDog.register() } returns Unit

        registerWatchDogsState.active() shouldBe Result.failure(testException)

        verifySuspend { lifecycleWatchDog.register() }
        verifySuspend { connectionWatchDog.register() }
    }
}