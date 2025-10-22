package com.emarsys.api.setup

import com.emarsys.TestEmarsysConfig
import com.emarsys.config.SdkConfig
import com.emarsys.context.SdkContextApi
import com.emarsys.core.exceptions.SdkException
import com.emarsys.core.log.Logger
import com.emarsys.disable.DisableOrganizerApi
import com.emarsys.enable.EnableOrganizerApi
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SetupTests {

    private lateinit var mockEnableOrganizer: EnableOrganizerApi
    private lateinit var mockDisableOrganizer: DisableOrganizerApi
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var mockLogger: Logger
    private lateinit var setup: SetupApi

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        mockEnableOrganizer = mock(MockMode.autofill)
        mockDisableOrganizer = mock(MockMode.autofill)
        mockSdkContext = mock()
        every { mockSdkContext.sdkDispatcher } returns StandardTestDispatcher()
        mockLogger = mock(MockMode.autofill)
        setup = Setup(mockEnableOrganizer, mockDisableOrganizer, mockSdkContext, mockLogger)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun enableTracking_shouldReturnSuccess_ifEnableOrganizer_succeeds() = runTest {
        val testConfig = TestEmarsysConfig("ABCDE-12345")

        val result = setup.enableTracking(testConfig)

        result.isSuccess shouldBe true
    }

    @Test
    fun enableTracking_shouldValidate_theConfig_andReturnFailure_ifValidationFails() = runTest {
        val testConfig = TestEmarsysConfig("null")

        val result = setup.enableTracking(testConfig)

        result.isFailure shouldBe true
        (result.exceptionOrNull() is SdkException.InvalidApplicationCodeException) shouldBe true
    }

    @Test
    fun enableTracking_shouldReturnFailure_ifEnableOrganizer_fails() = runTest {
        val testException = Exception("failed")
        val testConfig = TestEmarsysConfig("ABCDE-12345")
        everySuspend { mockEnableOrganizer.enableWithValidation(testConfig) } throws testException

        val result = setup.enableTracking(testConfig)

        result.isFailure shouldBe true
        result.exceptionOrNull() shouldBe testException
    }

    @Test
    fun disableTracking_shouldReturnSuccess_ifDisableOrganizer_succeeds() = runTest {
        val result = setup.disableTracking()

        result.isSuccess shouldBe true
    }

    @Test
    fun disableTracking_shouldReturnFailure_ifDisableOrganizer_fails() = runTest {
        val testException = Exception("failed")
        everySuspend { mockDisableOrganizer.disable() } throws testException

        val result = setup.disableTracking()

        result.isFailure shouldBe true
        result.exceptionOrNull() shouldBe testException
    }

    @Test
    fun testIsEnabled_shouldReturnTrue_ifAppCode_is_set() = runTest {
        val mockConfig: SdkConfig = mock()
        every { mockSdkContext.config } returns mockConfig
        every { mockConfig.applicationCode } returns "ABCDE-12345"
        val result = setup.isEnabled()

        result shouldBe true
    }

    @Test
    fun testIsEnabled_shouldReturnFalse_ifAppCode_is_null() = runTest {
        val mockConfig: SdkConfig = mock()
        every { mockSdkContext.config } returns mockConfig
        every { mockConfig.applicationCode } returns null

        val result = setup.isEnabled()

        result shouldBe false
    }
}