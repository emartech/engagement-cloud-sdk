package com.sap.ec.api.embeddedmessaging

import com.sap.ec.core.log.Logger
import com.sap.ec.mobileengage.embeddedmessaging.ui.list.ListPageViewModelApi
import com.sap.ec.networking.clients.embedded.messaging.model.MessageCategory
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class EmbeddedMessagingInternalTests {

    private lateinit var mockViewModel: ListPageViewModelApi
    private lateinit var mockLogger: Logger

    @BeforeTest
    fun setup() {
        mockViewModel = mock()
        mockLogger = mock(MockMode.autofill)
    }

    @Test
    fun categories_shouldReturn_categories_fromViewmodel() {
        val testCategories = listOf(MessageCategory(1, "1"), MessageCategory(2, "2"))
        every { mockViewModel.categories } returns MutableStateFlow(testCategories)

        val embeddedMessagingInternal = EmbeddedMessagingInternal(mockViewModel, mockLogger)

        embeddedMessagingInternal.categories shouldBe testCategories
    }

    @Test
    fun isUnreadFilterActive_shouldReturn_filterUnopenedOnly_fromViewmodel() {
        every { mockViewModel.filterUnopenedOnly } returns MutableStateFlow(false)

        val embeddedMessagingInternal = EmbeddedMessagingInternal(mockViewModel, mockLogger)

        embeddedMessagingInternal.isUnreadFilterActive shouldBe false
    }

    @Test
    fun activeCategoryFilters_shouldReturn_selectedCategories_fromViewmodel() {
        val testCategories = listOf(MessageCategory(1, "Category 1"), MessageCategory(2, "Category 2"))
        every { mockViewModel.selectedCategoryIds } returns MutableStateFlow(setOf(1))
        every { mockViewModel.categories } returns MutableStateFlow(testCategories)

        val embeddedMessagingInternal = EmbeddedMessagingInternal(mockViewModel, mockLogger)

        embeddedMessagingInternal.activeCategoryFilters shouldBe setOf(MessageCategory(1, "Category 1"))
    }

    @Test
    fun filterUnreadOnly_shouldCallMethod_setFilterUnreadOnly_withCorrectArgument_onViewmodel() {
        every { mockViewModel.setFilterUnopenedOnly(true) } returns Unit

        val embeddedMessagingInternal = EmbeddedMessagingInternal(mockViewModel, mockLogger)
        embeddedMessagingInternal.filterUnreadOnly(true)

        verify { mockViewModel.setFilterUnopenedOnly(true) }
    }

    @Test
    fun filterByCategories_shouldCallMethod_setSelectedCategoryIds_withCorrectArgument_onViewmodel() {
        val categories = setOf(MessageCategory(1, "Category 1"), MessageCategory(2, "Category 2"))
        every { mockViewModel.setSelectedCategoryIds(setOf(1,2)) } returns Unit

        val embeddedMessagingInternal = EmbeddedMessagingInternal(mockViewModel, mockLogger)
        embeddedMessagingInternal.filterByCategories(categories)

        verify { mockViewModel.setSelectedCategoryIds(setOf(1,2)) }
    }

    @Test
    fun activate_shouldLog() = runTest {
        val embeddedMessagingInternal = EmbeddedMessagingInternal(mock(), mockLogger)

        embeddedMessagingInternal.activate()

        verifySuspend { mockLogger.debug("EmbeddedMessagingInternal - activate", true) }
    }
}