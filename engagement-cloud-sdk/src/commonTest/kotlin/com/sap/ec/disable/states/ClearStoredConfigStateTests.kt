package com.sap.ec.disable.states

import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.log.Logger
import dev.mokkery.MockMode
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class ClearStoredConfigStateTests {
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var mockLogger: Logger
    private lateinit var clearStoredConfigState: ClearStoredConfigState

    @BeforeTest
    fun setup() {
        mockSdkContext = mock(MockMode.autofill)
        mockLogger = mock(MockMode.autofill)

        clearStoredConfigState = ClearStoredConfigState(mockSdkContext, mockLogger)
    }

    @Test
    fun active_shouldCall_clear_onConfigStore_andReturn_success() = runTest {
        clearStoredConfigState.active() shouldBe Result.success(Unit)

        verifySuspend { mockSdkContext.setSdkConfig(null) }
    }

    @Test
    fun active_shouldCall_return_failure_ifStoreOperation_fails() = runTest {
        val testException = Exception("failure")
        everySuspend { mockSdkContext.setSdkConfig(null) } throws testException

        clearStoredConfigState.active() shouldBe Result.failure(testException)
    }
}