package com.emarsys.api.embeddedmessaging

import com.emarsys.core.log.Logger
import com.emarsys.mobileengage.embeddedmessaging.ui.list.ListPageViewModelApi
import com.emarsys.networking.clients.embedded.messaging.model.MessageCategory
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
    fun activeCategoryIdFilters_shouldReturn_selectedCategoryIds_fromViewmodel() {
        every { mockViewModel.selectedCategoryIds } returns MutableStateFlow((emptySet()))

        val embeddedMessagingInternal = EmbeddedMessagingInternal(mockViewModel, mockLogger)

        embeddedMessagingInternal.activeCategoryIdFilters shouldBe emptySet<Int>()
    }

    @Test
    fun filterUnreadOnly_shouldCallMethod_setFilterUnreadOnly_withCorrectArgument_onViewmodel() {
        every { mockViewModel.setFilterUnopenedOnly(true) } returns Unit

        val embeddedMessagingInternal = EmbeddedMessagingInternal(mockViewModel, mockLogger)
        embeddedMessagingInternal.filterUnreadOnly(true)

        verify { mockViewModel.setFilterUnopenedOnly(true) }
    }

    @Test
    fun filterByCategoryIds_shouldCallMethod_setSelectedCategoryIds_withCorrectArgument_onViewmodel() {
        every { mockViewModel.setSelectedCategoryIds(setOf(1,2)) } returns Unit

        val embeddedMessagingInternal = EmbeddedMessagingInternal(mockViewModel, mockLogger)
        embeddedMessagingInternal.filterByCategoryIds(setOf(1,2))

        verify { mockViewModel.setSelectedCategoryIds(setOf(1,2)) }
    }

    @Test
    fun activate_shouldLog() = runTest {
        val embeddedMessagingInternal = EmbeddedMessagingInternal(mock(), mockLogger)

        embeddedMessagingInternal.activate()

        verifySuspend { mockLogger.debug("EmbeddedMessagingInternal - activate", true) }
    }
}