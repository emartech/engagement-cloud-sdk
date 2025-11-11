package com.emarsys.mobileengage.embeddedmessaging.ui.list

import com.emarsys.mobileengage.action.models.PresentableActionModel
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ListPageModelTests {

    @Test
    fun fetchMessages_shouldReturn_ListOfEmbeddedMessages() {
        val model = ListPageModel()

        val result = model.fetchMessages()

        result.size shouldBe 15

    }

    @Test
    fun fetchMessages_shouldReturn_MessagesWithCorrectProperties() {
        val model = ListPageModel()

        val result = model.fetchMessages()

        result.forEach { message ->
            message.defaultAction shouldBe null
            message.actions shouldBe emptyList<PresentableActionModel>()
            message.tags shouldBe emptyList()
            message.categoryIds shouldBe emptyList()
            message.receivedAt shouldBe 100000L
            message.expiresAt shouldBe 110000L
            message.properties shouldBe emptyMap()
            message.trackingInfo shouldBe "anything"
        }
    }
}