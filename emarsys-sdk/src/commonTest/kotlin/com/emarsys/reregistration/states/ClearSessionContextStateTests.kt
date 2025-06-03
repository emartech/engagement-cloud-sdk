package com.emarsys.reregistration.states

import com.emarsys.core.networking.context.RequestContext
import dev.mokkery.MockMode
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class ClearSessionContextStateTests {

    private lateinit var requestContext: RequestContext
    private lateinit var clearSessionContextState: ClearSessionContextState

    @BeforeTest
    fun setUp() {
        requestContext = RequestContext(
            contactToken = "testContactToken",
            refreshToken = "testRefreshToken",
        )
        clearSessionContextState =
            ClearSessionContextState(requestContext, sdkLogger = mock(MockMode.autofill))
    }

    @Test
    fun active_shouldClearContactToken_andRefreshToken() = runTest {
        clearSessionContextState.active()

        requestContext.contactToken shouldBe null
        requestContext.refreshToken shouldBe null
    }
}