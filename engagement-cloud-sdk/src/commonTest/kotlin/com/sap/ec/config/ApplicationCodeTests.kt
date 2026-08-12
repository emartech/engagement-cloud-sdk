package com.sap.ec.config

import com.sap.ec.core.exceptions.SdkException
import com.sap.ec.core.log.Logger
import dev.mokkery.MockMode
import dev.mokkery.mock
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class ApplicationCodeTests {
    private lateinit var mockLogger: Logger

    @BeforeTest
    fun setup() {
        mockLogger = mock(MockMode.autofill)
    }

    @Test
    fun validate_shouldPass_forStandardFormat() = runTest {
        shouldNotThrow<SdkException.InvalidApplicationCodeException> {
            ApplicationCode("ABCDE-12345").validate(mockLogger, null)
        }
    }

    @Test
    fun validate_shouldPass_forMultiRegionV1Format() = runTest {
        shouldNotThrow<SdkException.InvalidApplicationCodeException> {
            ApplicationCode("INS-S01-APP-ABC12").validate(mockLogger, null)
        }
    }

    @Test
    fun validate_shouldPass_forMultiRegionV2Format_withSPrefix() = runTest {
        shouldNotThrow<SdkException.InvalidApplicationCodeException> {
            ApplicationCode("S-AB123-0DB36").validate(mockLogger, null)
        }
    }

    @Test
    fun validate_shouldPass_forMultiRegionV2Format_withPPrefix() = runTest {
        shouldNotThrow<SdkException.InvalidApplicationCodeException> {
            ApplicationCode("P-CD456-1EF78").validate(mockLogger, null)
        }
    }

    @Test
    fun validate_shouldPass_whenMatchesRemoteConfigRegex() = runTest {
        val customRegex = "^CUSTOM\\.[A-Z0-9]+$".toRegex()

        shouldNotThrow<SdkException.InvalidApplicationCodeException> {
            ApplicationCode("CUSTOM.ABC123").validate(mockLogger, customRegex)
        }
    }

    @Test
    fun validate_shouldPass_forStandardFormat_whenRemoteConfigRegexIsSet() = runTest {
        val customRegex = "^CUSTOM\\.[A-Z0-9]+$".toRegex()

        shouldNotThrow<SdkException.InvalidApplicationCodeException> {
            ApplicationCode("ABCDE-12345").validate(mockLogger, customRegex)
        }
    }

    @Test
    fun validate_shouldThrow_forEmptyAppCode() = runTest {
        shouldThrow<SdkException.InvalidApplicationCodeException> {
            ApplicationCode("").validate(mockLogger, null)
        }
    }

    @Test
    fun validate_shouldThrow_forBlankAppCode() = runTest {
        shouldThrow<SdkException.InvalidApplicationCodeException> {
            ApplicationCode(" ").validate(mockLogger, null)
        }
    }

    @Test
    fun validate_shouldThrow_forInvalidFormat() = runTest {
        shouldThrow<SdkException.InvalidApplicationCodeException> {
            ApplicationCode("INVALID").validate(mockLogger, null)
        }
    }

    @Test
    fun validate_shouldThrow_whenNotMatchingAnyRegex() = runTest {
        val customRegex = "^CUSTOM\\.[A-Z0-9]+$".toRegex()

        shouldThrow<SdkException.InvalidApplicationCodeException> {
            ApplicationCode("INVALID").validate(mockLogger, customRegex)
        }
    }

    @Test
    fun validate_shouldThrow_forLowercaseStandardFormat() = runTest {
        shouldThrow<SdkException.InvalidApplicationCodeException> {
            ApplicationCode("abcde-12345").validate(mockLogger, null)
        }
    }

    @Test
    fun validate_shouldPass_forRemoteConfigRegex_whenNullRegex() = runTest {
        shouldThrow<SdkException.InvalidApplicationCodeException> {
            ApplicationCode("CUSTOM.ABC123").validate(mockLogger, null)
        }
    }
}
