package com.sap.ec.reregistration.states

import com.sap.ec.core.networking.context.RequestContextApi
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.verify
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class ClearRequestContextTokensStateTests {

    private lateinit var requestContext: RequestContextApi
    private lateinit var clearRequestContextTokensState: ClearRequestContextTokensState

    @BeforeTest
    fun setUp() {
        requestContext = mock(MockMode.autofill)
        every { requestContext.contactToken } returns "testContactToken"
        every { requestContext.refreshToken } returns "testRefreshToken"
        clearRequestContextTokensState =
            ClearRequestContextTokensState(requestContext, sdkLogger = mock(MockMode.autofill))
    }

    @Test
    fun active_shouldClearContactToken_andRefreshToken() = runTest {
        clearRequestContextTokensState.active() shouldBe Result.success(Unit)

        verify { requestContext.clearTokens() }
    }
}