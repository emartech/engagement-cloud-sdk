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

@OptIn(ExperimentalCoroutinesApi::class)
class IosEmbeddedMessagingTests {
    private companion object {
        val CATEGORIES = listOf(
            MessageCategory("1", "Category 1"),
            MessageCategory("2", "Category 2")
        )
    }

    private lateinit var mockEmbeddedMessaging: EmbeddedMessagingApi
    private lateinit var iosEmbeddedMessaging: IosEmbeddedMessagingApi

    @BeforeTest
    fun setup() {
        mockEmbeddedMessaging = mock(MockMode.autofill)
        iosEmbeddedMessaging = IosEmbeddedMessaging(mockEmbeddedMessaging)
    }

    @Test
    fun `categories should return same value as embeddedMessaging categories`() {
        every { mockEmbeddedMessaging.categories } returns CATEGORIES

        iosEmbeddedMessaging.categories shouldBe CATEGORIES
    }

    @Test
    fun `isUnreadFilterActive should return same value as embeddedMessaging`() {
        every { mockEmbeddedMessaging.isUnreadFilterActive } returns true

        iosEmbeddedMessaging.isUnreadFilterActive shouldBe true
    }

    @Test
    fun `activeCategoryFilters should return same value as embeddedMessaging`() {
        every { mockEmbeddedMessaging.activeCategoryFilters } returns CATEGORIES

        iosEmbeddedMessaging.activeCategoryFilters shouldBe CATEGORIES
    }

    @Test
    fun `filterUnreadOnly should delegate call to embeddedMessaging`() {
        iosEmbeddedMessaging.filterUnreadOnly(true)

        verify { mockEmbeddedMessaging.filterUnreadOnly(true) }
    }

    @Test
    fun `filterByCategories should delegate call to embeddedMessaging`() {
        iosEmbeddedMessaging.filterByCategories(CATEGORIES)

        verify { mockEmbeddedMessaging.filterByCategories(CATEGORIES) }
    }
}