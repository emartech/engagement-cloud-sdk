package com.sap.ec.init.states

import com.sap.ec.core.log.SdkLogger
import com.sap.ec.mobileengage.session.SessionApi
import com.sap.ec.watchdog.lifecycle.LifecycleWatchDog
import dev.mokkery.MockMode
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
    private lateinit var mockMobileEngageSession: SessionApi
    private lateinit var mockLifecycleWatchDog: LifecycleWatchDog

    @BeforeTest
    fun setup() {
        mockMobileEngageSession = mock()
        mockLifecycleWatchDog = mock()

        sessionSubscriptionState =
            SessionSubscriptionState(mockMobileEngageSession, mockLifecycleWatchDog, SdkLogger("TestLoggerName", mock(MockMode.autofill), sdkContext = mock()))
    }

    @Test
    fun testName() = runTest {
        sessionSubscriptionState.name shouldBe "sessionSubscriptionState"
    }

    @Test
    fun testActive_should_call_subscribe_on_mobileEngageSession() = runTest {
        everySuspend { mockMobileEngageSession.subscribe(mockLifecycleWatchDog) } returns Unit

        sessionSubscriptionState.active() shouldBe Result.success(Unit)

        verifySuspend { mockMobileEngageSession.subscribe(mockLifecycleWatchDog) }
    }
}