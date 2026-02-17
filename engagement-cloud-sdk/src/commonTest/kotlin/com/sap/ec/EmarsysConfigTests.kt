package com.sap.ec

import com.sap.ec.config.isValid
import com.sap.ec.core.exceptions.SdkException
import dev.mokkery.MockMode
import dev.mokkery.mock
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class EngagementCloudSDKConfigTest {

    @Test
    fun testEngagementCloudSDKConfig_isValid_shouldBe_true() = runTest {
        val config = TestEngagementCloudSDKConfig("ASD12-FGH34")

        config.applicationCode shouldBe "ASD12-FGH34"
        config.isValid(mock(mode = MockMode.autofill)) shouldBe true
    }

    @Test
    fun testEngagementCloudSDKConfig_isValid_shouldBe_false() = runTest {
        val config = TestEngagementCloudSDKConfig("null")

        config.applicationCode shouldBe "null"

        shouldThrow<SdkException.InvalidApplicationCodeException> {
            config.isValid(mock(mode = MockMode.autofill))
        }
    }
}