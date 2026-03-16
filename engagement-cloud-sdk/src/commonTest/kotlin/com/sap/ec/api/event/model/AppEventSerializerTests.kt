package com.sap.ec.api.event.model

import com.sap.ec.util.JsonUtil
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import kotlin.test.Test

class AppEventSerializerTests {

    private val json = JsonUtil.json

    @Test
    fun serialize_andDeserialize_shouldProduceEqualAppEvent_whenSourceIsPresent() {
        val original = AppEvent(
            id = "test-id-123",
            name = "testEvent",
            payload = mapOf("key" to "value")

        )

        val serialized = json.encodeToString(AppEvent.serializer(), original)
        val deserialized = json.decodeFromString(AppEvent.serializer(), serialized)

        deserialized shouldBe original
    }
}