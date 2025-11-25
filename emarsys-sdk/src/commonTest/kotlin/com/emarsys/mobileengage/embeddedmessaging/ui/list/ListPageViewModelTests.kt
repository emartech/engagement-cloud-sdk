package com.emarsys.mobileengage.embeddedmessaging.ui.list

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.util.DownloaderApi
import com.emarsys.networking.clients.embedded.messaging.model.EmbeddedMessage
import com.emarsys.networking.clients.embedded.messaging.model.MessageCategory
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ListPageViewModelTests {
    private companion object {
        val CATEGORIES = listOf(
            MessageCategory(id = 1, value = "Category 1"),
            MessageCategory(id = 2, value = "Category 2")
        )
    }

    private lateinit var mockModel: ListPageModelApi
    private lateinit var mockDownloaderApi: DownloaderApi
    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi
    private lateinit var viewModel: ListPageViewModel
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockModel = mock(MockMode.autofill)
        mockDownloaderApi = mock(MockMode.autofill)
        mockSdkEventDistributor = mock(MockMode.autofill)

        viewModel = ListPageViewModel(
            mockModel,
            mockDownloaderApi,
            mockSdkEventDistributor,
            CoroutineScope(SupervisorJob() + testDispatcher)
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testRefreshMessages_messagesAndCategories_shouldBeEmpty_when_ViewModelIsCreated() =
        runTest {
            everySuspend {
                mockModel.fetchMessagesWithCategories(
                    any(),
                    any()
                )
            } returns Result.success(MessagesWithCategories())

            val messagesResult = viewModel.messages.value
            val categoriesResult = viewModel.categories.value

            messagesResult shouldBe emptyList()
            categoriesResult shouldBe emptyList()
        }

    @Test
    fun testRefreshMessages_shouldSetMessagesAndCategories_when_refreshMessagesIsCalled() =
        runTest {
            val testMessages = listOf(
                createTestMessage(id = "1", title = "Title1", lead = "Lead1"),
                createTestMessage(id = "2", title = "Title2", lead = "Lead2")
            )

            everySuspend {
                mockModel.fetchMessagesWithCategories(
                    any(),
                    any()
                )
            } returns Result.success(
                MessagesWithCategories(
                    CATEGORIES,
                    testMessages
                )
            )

            viewModel.refreshMessages()
            viewModel.isRefreshing.value shouldBe true

            advanceUntilIdle()

            val messageResults = viewModel.messages.value
            messageResults.size shouldBe 2
            messageResults[0].id shouldBe "1"
            messageResults[0].title shouldBe "Title1"
            messageResults[0].lead shouldBe "Lead1"
            messageResults[1].id shouldBe "2"
            messageResults[1].title shouldBe "Title2"
            messageResults[1].lead shouldBe "Lead2"

            val categoryResults = viewModel.categories.value
            categoryResults.size shouldBe 2
            categoryResults shouldBe CATEGORIES
            viewModel.isRefreshing.value shouldBe false
        }

    @Test
    fun testRefreshMessages_shouldNotUpdateMessagesAndCategories_when_fetchMessagesWithCategoriesReturnsFailureResult() =
        runTest {
            val testMessages = listOf(
                createTestMessage(id = "1", title = "Title1", lead = "Lead1"),
                createTestMessage(id = "2", title = "Title2", lead = "Lead2")
            )
            everySuspend {
                mockModel.fetchMessagesWithCategories(
                    any(),
                    any()
                )
            } returns Result.success(
                MessagesWithCategories(
                    CATEGORIES,
                    testMessages
                )
            )

            viewModel.refreshMessages()

            advanceUntilIdle()

            val messageResult = viewModel.messages.value
            messageResult.size shouldBe 2
            val categoriesResult = viewModel.categories.value
            categoriesResult shouldBe CATEGORIES

            everySuspend {
                mockModel.fetchMessagesWithCategories(
                    any(),
                    any()
                )
            } returns Result.failure(
                RuntimeException("Network error")
            )

            viewModel.refreshMessages()

            advanceUntilIdle()

            val messageResultAfterFailure = viewModel.messages.value
            messageResultAfterFailure.size shouldBe 2
            val categoriesResultAfterFailure = viewModel.categories.value
            categoriesResultAfterFailure shouldBe CATEGORIES
            viewModel.isRefreshing.value shouldBe false
        }

    @Test
    fun testRefreshMessages_shouldUpdateMessagesAndCategories_when_refreshMessagesIsCalledMultipleTimes() =
        runTest {
            val firstMessages = listOf(createTestMessage(id = "1", title = "First"))
            val secondMessages = listOf(
                createTestMessage(id = "2", title = "Second"),
                createTestMessage(id = "3", title = "Third")
            )
            val newCategories = listOf(MessageCategory(id = 3, value = "Category 3"))

            everySuspend {
                mockModel.fetchMessagesWithCategories(
                    any(),
                    any()
                )
            } returns Result.success(
                MessagesWithCategories(
                    CATEGORIES,
                    firstMessages
                )
            )

            viewModel.refreshMessages()
            advanceUntilIdle()
            val firstResult = viewModel.messages.value
            firstResult.size shouldBe 1
            firstResult[0].id shouldBe "1"
            viewModel.categories.value shouldBe CATEGORIES

            everySuspend {
                mockModel.fetchMessagesWithCategories(
                    any(),
                    any()
                )
            } returns Result.success(
                MessagesWithCategories(
                    newCategories,
                    secondMessages
                )
            )
            viewModel.refreshMessages()
            advanceUntilIdle()
            val secondResult = viewModel.messages.value
            secondResult.size shouldBe 2
            secondResult[0].id shouldBe "2"
            secondResult[1].id shouldBe "3"
            viewModel.categories.value shouldBe newCategories
        }

    @Test
    fun testRefreshMessages_shouldCreateMessageItemViewModels_withCorrectProperties() = runTest {
        val testMessage = createTestMessage(
            id = "testId",
            title = "Test Title",
            lead = "Test Lead",
            imageUrl = "https://example.com/image.jpg",
            receivedAt = 1234567890L
        )
        everySuspend { mockModel.fetchMessagesWithCategories(any(), any()) } returns Result.success(
            MessagesWithCategories(
                emptyList(),
                listOf(testMessage)
            )
        )

        viewModel.refreshMessages()
        advanceUntilIdle()

        val result = viewModel.messages.value
        result.size shouldBe 1
        val messageViewModel = result[0]
        messageViewModel.id shouldBe "testId"
        messageViewModel.title shouldBe "Test Title"
        messageViewModel.lead shouldBe "Test Lead"
        messageViewModel.imageUrl shouldBe "https://example.com/image.jpg"
        messageViewModel.receivedAt shouldBe 1234567890L
    }

    @Test
    fun testRefreshMessages_shouldFetchMessages_withCurrentSelectedFilters() = runTest {
        val filterUnreadOnly = true
        val selectedCategoryIds = setOf(1)

        viewModel.setFilterUnreadOnly(filterUnreadOnly)
        viewModel.setSelectedCategoryIds(selectedCategoryIds)
        val testMessage = createTestMessage(
            id = "testId",
            title = "Test Title",
            lead = "Test Lead",
            imageUrl = "https://example.com/image.jpg",
            receivedAt = 1234567890L,
            categoryIds = listOf(1)
        )
        everySuspend { mockModel.fetchMessagesWithCategories(true, selectedCategoryIds.toList()) } returns Result.success(
            MessagesWithCategories(
                CATEGORIES,
                listOf(testMessage)
            )
        )

        viewModel.refreshMessages()

        advanceUntilIdle()

        val result = viewModel.messages.value
        result.size shouldBe 1
        val messageViewModel = result[0]
        with(messageViewModel) {
            id shouldBe "testId"
            title shouldBe "Test Title"
            lead shouldBe "Test Lead"
            imageUrl shouldBe "https://example.com/image.jpg"
            receivedAt shouldBe 1234567890L
            categoryIds shouldBe listOf(1)
        }
    }

    @Test
    fun isRefreshing_shouldBeFalse_whenViewModelIsCreated() = runTest {
        val result = viewModel.isRefreshing.value

        result shouldBe false
    }

    @Test
    fun isRefreshing_shouldBeTrue_whenRefreshStarts() = runTest {
        everySuspend { mockModel.fetchMessagesWithCategories(any(), any()) } returns Result.success(
            MessagesWithCategories()
        )

        viewModel.refreshMessages()

        val result = viewModel.isRefreshing.value
        result shouldBe true
    }

    @Test
    fun isRefreshing_shouldBeFalse_whenRefreshCompletes() = runTest {
        everySuspend { mockModel.fetchMessagesWithCategories(any(), any()) } returns Result.success(
            MessagesWithCategories()
        )

        viewModel.refreshMessages()

        advanceUntilIdle()

        val result = viewModel.isRefreshing.value
        result shouldBe false
    }

    @Test
    fun isRefreshing_shouldBeFalse_whenRefreshFails() = runTest {
        everySuspend { mockModel.fetchMessagesWithCategories(any(), any()) } returns Result.failure(
            RuntimeException("Network error")
        )

        viewModel.refreshMessages()

        advanceUntilIdle()

        val result = viewModel.isRefreshing.value
        result shouldBe false
    }

    @Test
    fun refreshMessages_shouldUpdateIsRefreshingState() = runTest {
        val testMessages = listOf(createTestMessage(id = "1", title = "Title1"))
        everySuspend { mockModel.fetchMessagesWithCategories(any(), any()) } returns Result.success(
            MessagesWithCategories(
                emptyList(),
                testMessages
            )
        )

        viewModel.isRefreshing.value shouldBe false

        viewModel.refreshMessages()
        viewModel.isRefreshing.value shouldBe true

        advanceUntilIdle()

        viewModel.isRefreshing.value shouldBe false
    }

    private fun createTestMessage(
        id: String = "1",
        title: String = "testTitle",
        lead: String = "testLead",
        imageUrl: String? = null,
        receivedAt: Long = 100000L,
        categoryIds: List<Int> = emptyList()
    ): EmbeddedMessage {
        return EmbeddedMessage(
            id = id,
            title = title,
            lead = lead,
            imageUrl = imageUrl,
            defaultAction = null,
            actions = emptyList(),
            tags = emptyList(),
            categoryIds = categoryIds,
            receivedAt = receivedAt,
            expiresAt = 110000L,
            properties = emptyMap(),
            trackingInfo = "anything"
        )
    }
}

