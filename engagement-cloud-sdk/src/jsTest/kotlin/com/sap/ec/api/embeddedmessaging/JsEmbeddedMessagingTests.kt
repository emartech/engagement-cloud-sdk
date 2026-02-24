package com.sap.ec.api.embeddedmessaging

import com.sap.ec.networking.clients.embedded.messaging.model.MessageCategory
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.verify
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalWasmJsInterop::class)
class JsEmbeddedMessagingTests {
    private companion object {
        val CATEGORIES = listOf(
            MessageCategory(1, "Category 1"),
            MessageCategory(2, "Category 2")
        )
    }

    private lateinit var mockEmbeddedMessaging: EmbeddedMessagingApi
    private lateinit var embeddedMessaging: JsEmbeddedMessagingApi

    @BeforeTest
    fun setup() {
        mockEmbeddedMessaging = mock(MockMode.autofill)
        embeddedMessaging = JsEmbeddedMessaging(mockEmbeddedMessaging)
    }

    @Test
    fun `categories should return same value as embeddedMessaging categories`() {
        every { mockEmbeddedMessaging.categories } returns CATEGORIES

        embeddedMessaging.getCategories() shouldBe CATEGORIES
    }

    @Test
    fun `isUnreadFilterActive should return same value as embeddedMessaging`() {
        every { mockEmbeddedMessaging.isUnreadFilterActive } returns true

        embeddedMessaging.isUnreadFilterActive() shouldBe true
    }

    @Test
    fun `getActiveCategoryFilters should return same value as embeddedMessaging`() {
        every { mockEmbeddedMessaging.activeCategoryFilters } returns CATEGORIES

        embeddedMessaging.getActiveCategoryFilters() shouldBe CATEGORIES.toJsArray()
    }

    @Test
    fun `filterUnreadOnly should delegate call to embeddedMessaging`() {
        embeddedMessaging.filterUnreadOnly(true)

        verify { mockEmbeddedMessaging.filterUnreadOnly(true) }
    }

    @Test
    fun `filterByCategories should delegate call to embeddedMessaging`() {
        embeddedMessaging.filterByCategories(CATEGORIES.toJsArray())

        verify { mockEmbeddedMessaging.filterByCategories(CATEGORIES) }
    }
}