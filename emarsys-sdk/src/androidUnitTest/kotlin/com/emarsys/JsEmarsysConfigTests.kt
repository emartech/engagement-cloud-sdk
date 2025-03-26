package com.emarsys

import com.emarsys.util.JsonUtil
import io.kotest.matchers.shouldBe
import org.junit.Test

class AndroidEmarsysConfigTests {

    @Test
    fun testSerialization() {
        val config = AndroidEmarsysConfig(
            applicationCode = "appCode",
            merchantId = "merchantId",
            sharedSecret = "testSharedSecret",
            launchActivityClass = TestClass::class.java
        )

        val serialized = JsonUtil.json.encodeToString(config)

        val deserialized = JsonUtil.json.decodeFromString<AndroidEmarsysConfig>(serialized)

        deserialized.applicationCode shouldBe config.applicationCode
        deserialized.merchantId shouldBe config.merchantId
        deserialized.sharedSecret shouldBe config.sharedSecret
        deserialized.launchActivityClass shouldBe config.launchActivityClass
    }
}