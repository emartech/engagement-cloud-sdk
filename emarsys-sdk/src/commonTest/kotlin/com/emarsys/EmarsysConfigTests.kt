package com.emarsys

import com.emarsys.core.exceptions.PreconditionFailedException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class EmarsysConfigTest {

    @Test
    fun testEmarsysConfig_isValid_shouldBe_true() {
        val config = TestEmarsysConfig("appId", "merchantId")

        config.applicationCode shouldBe "appId"
        config.merchantId shouldBe "merchantId"
        config.isValid() shouldBe true
    }

    @Test
    fun testEmarsysConfig_isValid_shouldBe_false() {
        val config = TestEmarsysConfig("null", "merchantId")

        config.applicationCode shouldBe "null"
        config.merchantId shouldBe "merchantId"

        shouldThrow<PreconditionFailedException> {
            config.isValid()
        }
    }
}