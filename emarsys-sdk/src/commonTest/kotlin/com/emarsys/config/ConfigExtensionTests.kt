package com.emarsys.config


import com.emarsys.TestEmarsysConfig
import com.emarsys.core.exceptions.PreconditionFailedException
import com.emarsys.core.log.Logger
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
        val testConfig = TestEmarsysConfig("test-application-code")

        testConfig.isValid(mockLogger) shouldBe true
    }

    @Test
    fun isValid_shouldThrow_preconditionFailedException_whenAppCode_isNull() = runTest {
        val testConfig = TestEmarsysConfig(null)

        val exception =
            shouldThrow<PreconditionFailedException> { testConfig.isValid(mock<Logger>()) }
        exception.message shouldBe "ApplicationCode must be present for Tracking!"
    }
}