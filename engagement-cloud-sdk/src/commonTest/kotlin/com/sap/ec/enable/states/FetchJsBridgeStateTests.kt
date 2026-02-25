package com.sap.ec.enable.states

import com.sap.ec.core.log.Logger
import com.sap.ec.networking.clients.jsbridge.JsBridgeClientApi
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class FetchJsBridgeStateTests {
    private lateinit var mockJsBridgeClient: JsBridgeClientApi
    private lateinit var mockSdkLogger: Logger
    private lateinit var state: FetchJsBridgeState

    @BeforeTest
    fun setup() {
        mockSdkLogger = mock(MockMode.autofill)
        mockJsBridgeClient = mock(MockMode.autofill)

        state = FetchJsBridgeState(
            jsBridgeClient = mockJsBridgeClient,
            sdkLogger = mockSdkLogger
        )
    }

    @Test
    fun active_shouldReturnSuccess_whenFetchAndVerifySucceeds() = runTest {
        everySuspend { mockJsBridgeClient.validateJSBridge() } returns Result.success(Unit)

        val result = state.active()

        result shouldBe Result.success(Unit)
        verifySuspend { mockJsBridgeClient.validateJSBridge() }
    }

    @Test
    fun active_shouldReturnFailure_whenFetchAndVerifyFails() = runTest {
        val error = Exception("fetch failed")
        everySuspend { mockJsBridgeClient.validateJSBridge() } returns Result.failure(error)

        val result = state.active()

        result.isFailure shouldBe true
    }

    @Test
    fun name_shouldBeFetchJsBridge() {
        state.name shouldBe "fetchJsBridge"
    }
}
