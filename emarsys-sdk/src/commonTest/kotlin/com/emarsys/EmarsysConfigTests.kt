package com.emarsys

import com.emarsys.core.exceptions.PreconditionFailed
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class EmarsysConfigTest {

    @Test
    fun testEmarsysConfig_isValid_shouldBe_true() {
        val config = EmarsysConfig("appId", "merchantId")

        config.applicationCode shouldBe "appId"
        config.merchantId shouldBe "merchantId"
        config.isValid() shouldBe true
    }

    @Test
    fun testEmarsysConfig_isValid_shouldBe_false() {
        val config = EmarsysConfig("null", "merchantId")

        config.applicationCode shouldBe "null"
        config.merchantId shouldBe "merchantId"

        shouldThrow<PreconditionFailed> {
            config.isValid()
        }
    }
}