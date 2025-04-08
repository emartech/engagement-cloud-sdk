package com.emarsys.init.states

import com.emarsys.core.log.ConsoleLogger
import com.emarsys.core.log.SdkLogger
import com.emarsys.mobileengage.session.Session
import com.emarsys.watchdog.lifecycle.LifecycleWatchDog
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class SessionSubscriptionStateTests {
    private lateinit var sessionSubscriptionState: SessionSubscriptionState
    private lateinit var mockMobileEngageSession: Session
    private lateinit var mockLifecycleWatchDog: LifecycleWatchDog

    @BeforeTest
    fun setup() {
        mockMobileEngageSession = mock()
        mockLifecycleWatchDog = mock()

        sessionSubscriptionState =
            SessionSubscriptionState(mockMobileEngageSession, mockLifecycleWatchDog, SdkLogger("TestLoggerName", ConsoleLogger()))
    }

    @Test
    fun testName() = runTest {
        sessionSubscriptionState.name shouldBe "sessionSubscriptionState"
    }

    @Test
    fun testActive_should_call_subscribe_on_mobileEngageSession() = runTest {
        everySuspend { mockMobileEngageSession.subscribe(mockLifecycleWatchDog) } returns Unit

        sessionSubscriptionState.active()

        verifySuspend { mockMobileEngageSession.subscribe(mockLifecycleWatchDog) }
    }
}