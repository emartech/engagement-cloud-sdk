package com.emarsys

import com.emarsys.config.isValid
import com.emarsys.core.exceptions.SdkException
import dev.mokkery.MockMode
import dev.mokkery.mock
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class EmarsysConfigTest {

    @Test
    fun testEmarsysConfig_isValid_shouldBe_true() = runTest {
        val config = TestEmarsysConfig("ASD12-FGH34", "merchantId")

        config.applicationCode shouldBe "ASD12-FGH34"
        config.merchantId shouldBe "merchantId"
        config.isValid(mock(mode = MockMode.autofill)) shouldBe true
    }

    @Test
    fun testEmarsysConfig_isValid_shouldBe_false() = runTest {
        val config = TestEmarsysConfig("null", "merchantId")

        config.applicationCode shouldBe "null"
        config.merchantId shouldBe "merchantId"

        shouldThrow<SdkException.InvalidApplicationCodeException> {
            config.isValid(mock(mode = MockMode.autofill))
        }
    }
}