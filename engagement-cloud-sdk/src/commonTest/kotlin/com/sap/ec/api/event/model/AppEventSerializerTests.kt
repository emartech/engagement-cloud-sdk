package com.sap.ec.api.event.model

import com.sap.ec.util.JsonUtil
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import kotlin.test.Test

class AppEventSerializerTests {

    private val json = JsonUtil.json

    @Test
    fun serialize_andDeserialize_shouldProduceEqualAppEvent_whenSourceIsPresent() {
        val original = AppEvent(
            id = "test-id-123",
            name = "testEvent",
            payload = mapOf("key" to "value"),
            source = EventSource.Push
        )

        val serialized = json.encodeToString(AppEvent.serializer(), original)
        val deserialized = json.decodeFromString(AppEvent.serializer(), serialized)

        deserialized shouldBe original
    }

    @Test
    fun serialize_andDeserialize_shouldProduceEqualAppEvent_whenSourceIsNull() {
        val original = AppEvent(
            id = "test-id-456",
            name = "testEventNoSource",
            payload = mapOf("key" to "value"),
            source = null
        )

        val serialized = json.encodeToString(AppEvent.serializer(), original)
        val deserialized = json.decodeFromString(AppEvent.serializer(), serialized)

        deserialized shouldBe original
    }

    @Test
    fun serialize_shouldEmitLowercaseStringValue_forPushSource() {
        val event = AppEvent(
            id = "test-id",
            name = "test",
            source = EventSource.Push
        )

        val serialized = json.encodeToString(AppEvent.serializer(), event)

        serialized shouldContain """"source":"push""""
    }

    @Test
    fun serialize_shouldEmitLowercaseStringValue_forInappSource() {
        val event = AppEvent(
            id = "test-id",
            name = "test",
            source = EventSource.InApp
        )

        val serialized = json.encodeToString(AppEvent.serializer(), event)

        serialized shouldContain """"source":"in_app""""
    }

    @Test
    fun serialize_shouldEmitLowercaseStringValue_forInlineInappSource() {
        val event = AppEvent(
            id = "test-id",
            name = "test",
            source = EventSource.InlineInApp
        )

        val serialized = json.encodeToString(AppEvent.serializer(), event)

        serialized shouldContain """"source":"inline_in_app""""
    }

    @Test
    fun serialize_shouldEmitLowercaseStringValue_forEmbeddedMessagingSource() {
        val event = AppEvent(
            id = "test-id",
            name = "test",
            source = EventSource.EmbeddedMessagingRichContent
        )

        val serialized = json.encodeToString(AppEvent.serializer(), event)

        serialized shouldContain """"source":"embedded_messaging_rich_content""""
    }

    @Test
    fun serialize_shouldEmitLowercaseStringValue_forOnEventSource() {
        val event = AppEvent(
            id = "test-id",
            name = "test",
            source = EventSource.OnEvent
        )

        val serialized = json.encodeToString(AppEvent.serializer(), event)

        serialized shouldContain """"source":"on_event""""
    }

    @Test
    fun serialize_shouldOmitSourceField_whenSourceIsNull() {
        val event = AppEvent(
            id = "test-id",
            name = "test",
            source = null
        )

        val serialized = json.encodeToString(AppEvent.serializer(), event)

        serialized shouldNotContain "source"
    }

    @Test
    fun deserialize_shouldReturnNull_whenSourceFieldIsMissing() {
        val jsonString = """{"id":"test-id","name":"testEvent","type":"app_event"}"""

        val deserialized = json.decodeFromString(AppEvent.serializer(), jsonString)

        deserialized.source shouldBe null
    }

    @Test
    fun deserialize_shouldReturnNull_whenSourceValueIsUnknown() {
        val jsonString = """{"id":"test-id","name":"testEvent","source":"unknown_source","type":"app_event"}"""

        val deserialized = json.decodeFromString(AppEvent.serializer(), jsonString)

        deserialized.source shouldBe null
    }

    @Test
    fun deserialize_shouldMapCorrectly_forAllValidSourceValues() {
        val pushJson = """{"id":"1","name":"test","source":"push","type":"app_event"}"""
        val inappJson = """{"id":"2","name":"test","source":"in_app","type":"app_event"}"""
        val onEventJson = """{"id":"3","name":"test","source":"on_event","type":"app_event"}"""

        val pushEvent = json.decodeFromString(AppEvent.serializer(), pushJson)
        val inappEvent = json.decodeFromString(AppEvent.serializer(), inappJson)
        val onEventEvent = json.decodeFromString(AppEvent.serializer(), onEventJson)

        pushEvent.source shouldBe EventSource.Push
        inappEvent.source shouldBe EventSource.InApp
        onEventEvent.source shouldBe EventSource.OnEvent
    }
}
