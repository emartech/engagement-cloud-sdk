package com.emarsys.networking.clients.embedded.messaging.serializer

import com.emarsys.networking.clients.embedded.messaging.model.*
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import kotlin.test.Test

class MessagesResponseSerializerTests {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        classDiscriminator = "type"
    }

    @Test
    fun testMessagesResponseDeserializationWithCategoryLookup() {
        val rawJson = """
            {
                "version": "1",
                "top": 1,
                "meta": {
                    "categories": [
                        { "id": 1, "value": "Category 1" },
                        { "id": 2, "value": "Category 2" }
                    ]
                },
                "messages": [
                    {
                        "id": "message-id-1",
                        "title": "Test Message",
                        "lead": "This is a test message.",
                        "listThumbnailImage": null,
                        "defaultAction": null,
                        "actions": [],
                        "tags": ["tag1", "tag2"],
                        "categoryIds": [1],
                        "receivedAt": 1678886400000,
                        "expiresAt": null,
                        "properties": {},
                        "trackingInfo": "tracking-info"
                    }
                ]
            }
        """.trimIndent()

        val expectedResponse = MessagesResponse(
            version = "1",
            top = 1,
            meta = Meta(
                categories = listOf(
                    MessageCategory(1, "Category 1"),
                    MessageCategory(2, "Category 2")
                )
            ),
            messages = listOf(
                EmbeddedMessage(
                    id = "message-id-1",
                    title = "Test Message",
                    lead = "This is a test message.",
                    listThumbnailImage = null,
                    defaultAction = null,
                    actions = emptyList(),
                    tags = listOf("tag1", "tag2"),
                    categories = listOf(Category(1, "Category 1")),
                    receivedAt = 1678886400000L,
                    expiresAt = null,
                    properties = emptyMap(),
                    trackingInfo = "tracking-info"
                )
            )
        )

        val actualResponse = json.decodeFromString<MessagesResponse>(rawJson)

        expectedResponse shouldBe actualResponse
    }

    @Test
    fun testMessagesResponseSerialization() {
        val messagesResponse = MessagesResponse(
            version = "1",
            top = 1,
            meta = Meta(
                categories = listOf(
                    MessageCategory(1, "Category 1"),
                    MessageCategory(2, "Category 2")
                )
            ),
            messages = listOf(
                EmbeddedMessage(
                    id = "message-id-1",
                    title = "Test Message",
                    lead = "This is a test message.",
                    listThumbnailImage = null,
                    defaultAction = null,
                    actions = emptyList(),
                    tags = listOf("tag1", "tag2"),
                    categories = listOf(Category(1, "Category 1")),
                    receivedAt = 1678886400000L,
                    expiresAt = null,
                    properties = emptyMap(),
                    trackingInfo = "tracking-info"
                )
            )
        )

        val rawJson = json.encodeToString(MessagesResponse.serializer(), messagesResponse)
        val deserializedResponse = json.decodeFromString<MessagesResponse>(rawJson)

        messagesResponse shouldBe deserializedResponse
    }
}
