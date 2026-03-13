package com.sap.ec.networking.clients.embedded.messaging.model

import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import kotlin.test.Test

class MessageCategoryTests {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Test
    fun `text property should hold the display text`() {
        val category = MessageCategory("1", "Category 1")

        category.text shouldBe "Category 1"
    }

    @Test
    fun `deserialization from JSON with value field should map to text property`() {
        val rawJson = """{"id": "1", "value": "Category 1"}"""

        val category = json.decodeFromString<MessageCategory>(rawJson)

        category.id shouldBe "1"
        category.text shouldBe "Category 1"
    }

    @Test
    fun `serialization should produce value field in JSON for wire compatibility`() {
        val category = MessageCategory("1", "Category 1")

        val serialized = json.encodeToString(MessageCategory.serializer(), category)

        serialized shouldBe """{"id":"1","value":"Category 1"}"""
    }
}
