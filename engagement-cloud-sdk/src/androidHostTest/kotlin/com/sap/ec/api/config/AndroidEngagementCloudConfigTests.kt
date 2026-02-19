package com.sap.ec.api.config

import com.sap.ec.TestClass
import com.sap.ec.util.JsonUtil
import io.kotest.matchers.shouldBe
import org.junit.Test

class AndroidEngagementCloudSDKConfigTests {

    @Test
    fun testSerialization() {
        val config = AndroidEngagementCloudSDKConfig(
            applicationCode = "appCode",
            launchActivityClass = TestClass::class.java
        )

        val serialized = JsonUtil.json.encodeToString(config)

        val deserialized = JsonUtil.json.decodeFromString<AndroidEngagementCloudSDKConfig>(serialized)

        deserialized.applicationCode shouldBe config.applicationCode
        deserialized.launchActivityClass shouldBe config.launchActivityClass
    }
}