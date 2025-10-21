package com.emarsys.api.setup


import com.emarsys.AndroidEmarsysConfig
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class AndroidSetupTests {

    private lateinit var mockSetup: SetupApi
    private lateinit var androidSetup: AndroidSetupApi

    @BeforeTest
    fun setup() {
        mockSetup = mock(MockMode.autofill)
        androidSetup = AndroidSetup(mockSetup)
    }

    @Test
    fun enableTracking_shouldDelegate_toSetupApi_andReturnItsResult() = runTest {
        val testConfig = AndroidEmarsysConfig("ABC-123")
        val testResult = Result.success(Unit)
        everySuspend { mockSetup.enableTracking(testConfig) } returns testResult

        val result = androidSetup.enableTracking(testConfig)

        everySuspend { mockSetup.enableTracking(testConfig) }

        result shouldBe testResult
    }

    @Test
    fun disableTracking_shouldDelegate_toSetupApi_andReturnItsResult() = runTest {
        val testResult = Result.failure<Unit>(Exception("failure"))
        everySuspend { mockSetup.disableTracking() } returns testResult

        val result = androidSetup.disableTracking()

        everySuspend { mockSetup.disableTracking() }

        result shouldBe testResult
    }
}