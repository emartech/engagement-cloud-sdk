package com.emarsys.disable.states

import com.emarsys.config.SdkConfig
import com.emarsys.core.log.Logger
import com.emarsys.enable.config.SdkConfigStoreApi
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class ClearStoredConfigStateTests {
    private lateinit var mockConfigStore: SdkConfigStoreApi<SdkConfig>
    private lateinit var mockLogger: Logger
    private lateinit var clearStoredConfigState: ClearStoredConfigState

    @BeforeTest
    fun setup() {
        mockConfigStore = mock()
        mockLogger = mock(MockMode.autofill)

        clearStoredConfigState = ClearStoredConfigState(mockConfigStore, mockLogger)
    }

    @Test
    fun active_shouldCall_clear_onConfigStore_andReturn_success() = runTest {
        everySuspend { mockConfigStore.clear() } returns Unit

        clearStoredConfigState.active() shouldBe Result.success(Unit)
    }

    @Test
    fun active_shouldCall_return_failure_ifStoreOperation_fails() = runTest {
        val testException = Exception("failure")
        everySuspend { mockConfigStore.clear() } throws testException

        clearStoredConfigState.active() shouldBe Result.failure(testException)
    }
}