package com.emarsys.mobileengage.embedded.messages

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class EmbeddedMessagingPaginationStateTest {

    @Test
    fun canFetchNextPage_shouldReturnFalse_when_endReached() {
        val state = EmbeddedMessagingPaginationState(
            lastFetchMessagesId = "any",
            top = 30,
            offset = 0,
            categoryIds = listOf(1),
            receivedCount = 60,
            endReached = true
        )
        state.canFetchNextPage() shouldBe false
    }

    @Test
    fun canFetchNextPage_shouldReturnTrue_when_not_endReached() {
        val state = EmbeddedMessagingPaginationState(
            lastFetchMessagesId = "any",
            top = 30,
            offset = 0,
            categoryIds = listOf(1),
            receivedCount = 30,
            endReached = false
        )
        state.canFetchNextPage() shouldBe true
    }

    @Test
    fun updateOffset_shouldSetOffsetToReceivedCount() {
        val state = EmbeddedMessagingPaginationState(
            lastFetchMessagesId = "any",
            top = 20,
            offset = 0,
            categoryIds = listOf(1),
            receivedCount = 55,
            endReached = false
        )
        state.updateOffset()
        state.offset shouldBe 55
    }

    @Test
    fun reset_shouldSetAllPropertiesToDefaultValues() {
        val state = EmbeddedMessagingPaginationState(
            lastFetchMessagesId = "test-id",
            top = 50,
            offset = 25,
            categoryIds = listOf(1, 2, 3),
            receivedCount = 75,
            endReached = true
        )

        state.reset()

        state.lastFetchMessagesId shouldBe null
        state.top shouldBe 0
        state.offset shouldBe 0
        state.categoryIds shouldBe emptyList()
        state.receivedCount shouldBe 0
        state.endReached shouldBe false
    }

    @Test
    fun reset_shouldMakeCanFetchNextPageReturnTrue() {
        val state = EmbeddedMessagingPaginationState(endReached = true)

        state.reset()

        state.canFetchNextPage() shouldBe true
    }

    @Test
    fun reset_shouldWorkWithDefaultConstructor() {
        val state = EmbeddedMessagingPaginationState()
        state.lastFetchMessagesId = "modified"
        state.top = 100
        state.offset = 50
        state.categoryIds = listOf(5, 6)
        state.receivedCount = 25
        state.endReached = true

        state.reset()

        state.lastFetchMessagesId shouldBe null
        state.top shouldBe 0
        state.offset shouldBe 0
        state.categoryIds shouldBe emptyList()
        state.receivedCount shouldBe 0
        state.endReached shouldBe false
    }

    @Test
    fun reset_shouldAllowSubsequentOperations() {
        val state = EmbeddedMessagingPaginationState(
            receivedCount = 100,
            endReached = true
        )

        state.reset()

        state.receivedCount = 20
        state.updateOffset()
        state.offset shouldBe 20
        state.canFetchNextPage() shouldBe true
    }
}