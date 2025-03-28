package com.emarsys.init.states

import com.emarsys.core.Registerable
import com.emarsys.core.log.ConsoleLogger
import com.emarsys.core.log.SdkLogger
import dev.mokkery.answering.returns
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
            SdkLogger(ConsoleLogger())
        )
    }

    @Test
    fun testName() = runTest {
        registerWatchDogsState.name shouldBe "registerWatchdogsState"
    }

    @Test
    fun testActive_should_call_register_on_watchdogs() = runTest {
        everySuspend { lifecycleWatchDog.register() } returns Unit
        everySuspend { connectionWatchDog.register() } returns Unit

        registerWatchDogsState.active()

        verifySuspend { lifecycleWatchDog.register() }
        verifySuspend { connectionWatchDog.register() }
    }
}