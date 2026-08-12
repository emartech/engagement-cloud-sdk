package com.sap.ec.config


import com.sap.ec.TestEngagementCloudSDKConfig
import com.sap.ec.core.exceptions.SdkException
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
        mockLogger = mock(MockMode.autofill)
    }

    @Test
    fun isValid_shouldReturnTrue_forStandardFormat() = runTest {
        val testConfig = TestEngagementCloudSDKConfig("ABCDE-12345")

        testConfig.isValid(mockLogger, null) shouldBe true
    }

    @Test
    fun isValid_shouldReturnTrue_forMultiRegionV1Format() = runTest {
        val testConfig = TestEngagementCloudSDKConfig("INS-S01-APP-ABC12")

        testConfig.isValid(mockLogger, null) shouldBe true
    }

    @Test
    fun isValid_shouldReturnTrue_forMultiRegionV2Format_withSPrefix() = runTest {
        val testConfig = TestEngagementCloudSDKConfig("S-AB123-0DB36")

        testConfig.isValid(mockLogger, null) shouldBe true
    }

    @Test
    fun isValid_shouldReturnTrue_forMultiRegionV2Format_withPPrefix() = runTest {
        val testConfig = TestEngagementCloudSDKConfig("P-CD456-1EF78")

        testConfig.isValid(mockLogger, null) shouldBe true
    }

    @Test
    fun isValid_shouldReturnTrue_whenMatchesRemoteConfigRegex() = runTest {
        val customRegex = "^CUSTOM\\.[A-Z0-9]+$".toRegex()
        val testConfig = TestEngagementCloudSDKConfig("CUSTOM.ABC123")

        testConfig.isValid(mockLogger, customRegex) shouldBe true
    }

    @Test
    fun isValid_shouldReturnTrue_forStandardFormat_whenRemoteConfigRegexIsSet() = runTest {
        val customRegex = "^CUSTOM\\.[A-Z0-9]+$".toRegex()
        val testConfig = TestEngagementCloudSDKConfig("ABCDE-12345")

        testConfig.isValid(mockLogger, customRegex) shouldBe true
    }

    @Test
    fun isValid_shouldThrow_forEmptyAppCode() = runTest {
        val testConfig = TestEngagementCloudSDKConfig(" ")

        shouldThrow<SdkException.InvalidApplicationCodeException> {
            testConfig.isValid(mockLogger, null)
        }
    }

    @Test
    fun isValid_shouldThrow_forInvalidAppCode() = runTest {
        val testConfig = TestEngagementCloudSDKConfig("INVALID")

        shouldThrow<SdkException.InvalidApplicationCodeException> {
            testConfig.isValid(mockLogger, null)
        }
    }

    @Test
    fun isValid_shouldThrow_whenNotMatchingRemoteConfigRegex_andNotStandardFormat() = runTest {
        val customRegex = "^CUSTOM\\.[A-Z0-9]+$".toRegex()
        val testConfig = TestEngagementCloudSDKConfig("INVALID")

        shouldThrow<SdkException.InvalidApplicationCodeException> {
            testConfig.isValid(mockLogger, customRegex)
        }
    }
}