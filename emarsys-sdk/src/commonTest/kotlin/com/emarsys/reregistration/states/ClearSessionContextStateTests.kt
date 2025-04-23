package com.emarsys.reregistration.states

import com.emarsys.core.session.SessionContext
import dev.mokkery.MockMode
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class ClearSessionContextStateTests {

    private lateinit var sessionContext: SessionContext
    private lateinit var clearSessionContextState: ClearSessionContextState

    @BeforeTest
    fun setUp() {
        sessionContext = SessionContext(
            contactToken = "testContactToken",
            refreshToken = "testRefreshToken",
        )
        clearSessionContextState =
            ClearSessionContextState(sessionContext, sdkLogger = mock(MockMode.autofill))
    }

    @Test
    fun active_shouldClearContactToken_andRefreshToken() = runTest {
        clearSessionContextState.active()

        sessionContext.contactToken shouldBe null
        sessionContext.refreshToken shouldBe null
    }
}