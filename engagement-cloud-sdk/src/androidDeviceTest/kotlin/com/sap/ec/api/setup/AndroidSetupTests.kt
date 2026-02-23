package com.sap.ec.api.setup


import com.sap.ec.api.config.AndroidEngagementCloudSDKConfig
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
        val testConfig = AndroidEngagementCloudSDKConfig("ABC-123")
        val testResult = Result.success(Unit)
        everySuspend { mockSetup.enable(testConfig) } returns testResult

        val result = androidSetup.enable(testConfig)

        everySuspend { mockSetup.enable(testConfig) }
        result shouldBe testResult
    }

    @Test
    fun disableTracking_shouldDelegate_toSetupApi_andReturnItsResult() = runTest {
        val testResult = Result.failure<Unit>(Exception("failure"))
        everySuspend { mockSetup.disable() } returns testResult

        val result = androidSetup.disable()

        everySuspend { mockSetup.disable() }
        result shouldBe testResult
    }
}