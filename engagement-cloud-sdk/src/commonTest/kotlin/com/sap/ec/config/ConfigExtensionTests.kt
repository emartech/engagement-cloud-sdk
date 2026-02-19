package com.sap.ec.config


import com.sap.ec.TestEngagementCloudSDKConfig
import com.sap.ec.core.exceptions.SdkException.PreconditionFailedException
import com.sap.ec.core.log.Logger
import dev.mokkery.MockMode
import dev.mokkery.mock
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class ConfigExtensionTests {
    private lateinit var mockLogger: Logger

    @BeforeTest
    fun setup() {
        mockLogger = mock<Logger>(MockMode.autofill)
    }

    @Test
    fun isValid_shouldReturn_true() = runTest {
        val testConfig = TestEngagementCloudSDKConfig("INS-S01-APP-ABC12")

        testConfig.isValid(mockLogger) shouldBe true
    }

    @Test
    fun isValid_shouldThrow_preconditionFailedException_whenAppCode_isNull() = runTest {
        val testConfig = TestEngagementCloudSDKConfig(null)

        val exception =
            shouldThrow<PreconditionFailedException> { testConfig.isValid(mock<Logger>()) }
        exception.message shouldBe "ApplicationCode must be present for Tracking!"
    }
}