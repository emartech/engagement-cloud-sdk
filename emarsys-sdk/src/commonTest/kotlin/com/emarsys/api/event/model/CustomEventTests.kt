package com.emarsys.api.event.model

import com.emarsys.event.SdkEvent
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class CustomEventTests {
    private companion object {
        const val TEST_NAME = "testName"
        const val TEST_UUID = "testUUID"
        val TEST_TIMESTAMP = Instant.DISTANT_FUTURE
    }

    @Test
    fun toSdkEvent_shouldNotAddAttribute_ifAttributeProperty_isNull() {

        val testEvent = CustomEvent(TEST_NAME, attributes = null)

        val sdkEvent = testEvent.toSdkEvent(TEST_UUID, TEST_TIMESTAMP) as SdkEvent.External.Custom

        sdkEvent.attributes shouldBe null
    }

    @Test
    fun toSdkEvent_shouldAddAttribute_ifAttributeProperty_isNotNull() {
        val testAttributes = mapOf("key" to "value", "testKey" to "testValue")

        val testEvent = CustomEvent(TEST_NAME, attributes = testAttributes)

        val sdkEvent = testEvent.toSdkEvent(TEST_UUID, TEST_TIMESTAMP) as SdkEvent.External.Custom

        sdkEvent.attributes shouldBe buildJsonObject {
            put("key", "value")
            put("testKey", "testValue")
        }
    }
}