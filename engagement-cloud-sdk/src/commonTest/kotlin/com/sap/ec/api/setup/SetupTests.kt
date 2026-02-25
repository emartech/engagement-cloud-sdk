package com.sap.ec.api.setup

import com.sap.ec.TestEngagementCloudSDKConfig
import com.sap.ec.config.LinkContactData
import com.sap.ec.config.SdkConfig
import com.sap.ec.context.DefaultUrls
import com.sap.ec.context.SdkContext
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.exceptions.SdkException
import com.sap.ec.core.log.LogLevel
import com.sap.ec.core.log.Logger
import com.sap.ec.disable.DisableOrganizerApi
import com.sap.ec.enable.EnableOrganizerApi
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SetupTests {

    private lateinit var mockEnableOrganizer: EnableOrganizerApi
    private lateinit var mockDisableOrganizer: DisableOrganizerApi
    private lateinit var testSdkContext: SdkContextApi
    private lateinit var mockLogger: Logger
    private lateinit var mockOnContactLinkingFailedCallback: suspend () -> LinkContactData?
    private lateinit var setup: SetupApi

    @BeforeTest
    fun setup() {
        val mainDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(mainDispatcher)
        mockEnableOrganizer = mock(MockMode.autofill)
        mockDisableOrganizer = mock(MockMode.autofill)
        testSdkContext = SdkContext(
            sdkDispatcher = StandardTestDispatcher(),
            mainDispatcher = mainDispatcher,
            defaultUrls = DefaultUrls("", "", "", "", "", "", "", ""),
            remoteLogLevel = LogLevel.Error,
            features = mutableSetOf(),
            logBreadcrumbsQueueSize = 10,
            onContactLinkingFailed = null
        )
        mockOnContactLinkingFailedCallback = mock()
        mockLogger = mock(MockMode.autofill)
        setup = Setup(mockEnableOrganizer, mockDisableOrganizer, testSdkContext, mockLogger)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun enableTracking_shouldReturnSuccess_ifEnableOrganizer_succeeds() = runTest {
        val testConfig = TestEngagementCloudSDKConfig("ABCDE-12345")

        val result = setup.enable(testConfig, mockOnContactLinkingFailedCallback)

        result.isSuccess shouldBe true
        testSdkContext.onContactLinkingFailed shouldBe mockOnContactLinkingFailedCallback
    }

    @Test
    fun enable_shouldValidate_theConfig_andReturnFailure_ifValidationFails() = runTest {
        val testConfig = TestEngagementCloudSDKConfig("null")

        val result = setup.enable(testConfig, mockOnContactLinkingFailedCallback)

        result.isFailure shouldBe true
        (result.exceptionOrNull() is SdkException.InvalidApplicationCodeException) shouldBe true
    }

    @Test
    fun enableTracking_shouldReturnFailure_ifEnableOrganizer_fails() = runTest {
        val testException = Exception("failed")
        val testConfig = TestEngagementCloudSDKConfig("ABCDE-12345")
        everySuspend { mockEnableOrganizer.enableWithValidation(testConfig) } throws testException

        val result = setup.enable(testConfig, mockOnContactLinkingFailedCallback)

        result.isFailure shouldBe true
        result.exceptionOrNull() shouldBe testException
    }

    @Test
    fun disableTracking_shouldReturnSuccess_ifDisableOrganizer_succeeds() = runTest {
        val result = setup.disable()

        result.isSuccess shouldBe true
    }

    @Test
    fun disableTracking_shouldReturnFailure_ifDisableOrganizer_fails() = runTest {
        val testException = Exception("failed")
        everySuspend { mockDisableOrganizer.disable() } throws testException

        val result = setup.disable()

        result.isFailure shouldBe true
        result.exceptionOrNull() shouldBe testException
    }

    @Test
    fun testIsEnabled_shouldReturnTrue_ifAppCode_is_set() = runTest {
        val mockConfig: SdkConfig = mock()
        testSdkContext.config = mockConfig
        every { mockConfig.applicationCode } returns "ABCDE-12345"
        val result = setup.isEnabled()

        result shouldBe true
    }
}