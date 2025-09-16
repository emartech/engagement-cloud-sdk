package com.emarsys.mobileengage.embedded.messages

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class EmbeddedMessagingPaginationStateTest {

    @Test
    fun testCanFetchNextPage_shouldReturnFalse_when_onLastPage(){
        val actualState = EmbeddedMessagingPaginationState(
            lastFetchMessagesId = "any",
            top = 30,
            offset = 0,
            categoryIds = listOf(1),
            count = 30
        )

        val result = actualState.canFetchNextPage()
        result shouldBe false
    }

    @Test
    fun testCanFetchNextPage_shouldReturnTrue_when_not_onLastPage(){
        val actualState = EmbeddedMessagingPaginationState(
            lastFetchMessagesId = "any",
            top = 30,
            offset = 0,
            categoryIds = listOf(1),
            count = 31
        )

        val result = actualState.canFetchNextPage()
        result shouldBe true
    }

    @Test
    fun testCanFetchNextPage_shouldReturnFalse_when_top_or_offset_or_count_is_less_than_0(){
        val actualState = EmbeddedMessagingPaginationState(
            lastFetchMessagesId = "any",
            top = 20,
            offset = -1,
            categoryIds = listOf(1),
            count = 30
        )

        val result = actualState.canFetchNextPage()

        result shouldBe false
    }

    @Test
    fun updateOffset_should_not_do_anything_when_offset_or_top_is_less_than_0(){
        val expectedOffset = 0

        val actualState = EmbeddedMessagingPaginationState(
            lastFetchMessagesId = "any",
            top = -1,
            offset = 0,
            categoryIds = listOf(1),
            count = 30
        )

        actualState.updateOffset()

        actualState.offset shouldBe expectedOffset
    }
}