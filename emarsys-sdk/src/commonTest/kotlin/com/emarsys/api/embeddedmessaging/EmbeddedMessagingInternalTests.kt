package com.emarsys.api.embeddedmessaging

import com.emarsys.core.log.Logger
import com.emarsys.mobileengage.embeddedmessaging.ui.list.ListPageViewModelApi
import com.emarsys.networking.clients.embedded.messaging.model.MessageCategory
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class EmbeddedMessagingInternalTests {

    @Test
    fun categories_shouldReturn_categories_fromViewmodel() {
        val testCategories = listOf(MessageCategory(1, "1"), MessageCategory(2, "2"))
        val mockViewModel: ListPageViewModelApi = mock()
        every { mockViewModel.categories } returns MutableStateFlow(testCategories)
        val embeddedMessagingInternal = EmbeddedMessagingInternal(mockViewModel, mock())

        embeddedMessagingInternal.categories shouldBe testCategories
    }

    @Test
    fun activate_shouldLog() = runTest {
        val mockLogger: Logger = mock(MockMode.autofill)
        val embeddedMessagingInternal = EmbeddedMessagingInternal(mock(), mockLogger)

        embeddedMessagingInternal.activate()

        verifySuspend { mockLogger.debug("EmbeddedMessagingInternal - activate", true) }
    }
}