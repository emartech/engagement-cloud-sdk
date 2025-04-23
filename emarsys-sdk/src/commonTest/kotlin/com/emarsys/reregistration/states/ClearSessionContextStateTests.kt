package com.emarsys.reregistration.states

import com.emarsys.core.session.SessionContextApi
import dev.mokkery.MockMode
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class ClearSessionContextStateTests {

    private lateinit var sessionContext: SessionContextApi
    private lateinit var clearSessionContextState: ClearSessionContextState

    @BeforeTest
    fun setUp() {
        sessionContext = mock(MockMode.autofill)
        clearSessionContextState = ClearSessionContextState(sessionContext)
    }

    @Test
    fun active_shouldClearContactToken_andRefreshToken() = runTest {
        clearSessionContextState.active()

        verifySuspend { sessionContext.clearSessionTokens() }
    }
}