package com.emarsys.mobileengage.embeddedmessaging.ui.list

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.util.DownloaderApi
import com.emarsys.mobileengage.action.models.PresentableActionModel
import com.emarsys.networking.clients.embedded.messaging.model.EmbeddedMessage
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
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
    private lateinit var mockModel: ListPageModelApi
    private lateinit var mockDownloaderApi: DownloaderApi
    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() = runTest {
        Dispatchers.setMain(testDispatcher)
        mockModel = mock(MockMode.autofill)
        mockDownloaderApi = mock(MockMode.autofill)
        mockSdkEventDistributor = mock(MockMode.autofill)
    }

    @AfterTest
    fun tearDown() = runTest {
        Dispatchers.resetMain()
    }

    @Test
    fun messages_shouldBeEmpty_when_ViewModelIsCreated() = runTest(testDispatcher) {
        everySuspend { mockModel.fetchMessages() } returns emptyList()
        val viewModel = createViewModel()

        val result = viewModel.messages.value

        result shouldBe emptyList()
    }

    @Test
    fun messages_shouldContainMessageViewModels_when_refreshMessagesIsCalled() = runTest(testDispatcher) {
        val testMessages = listOf(
            createTestMessage(id = "1", title = "Title1", lead = "Lead1"),
            createTestMessage(id = "2", title = "Title2", lead = "Lead2")
        )
        everySuspend { mockModel.fetchMessages() } returns testMessages
        val viewModel = createViewModel()

        viewModel.refreshMessages()
        advanceUntilIdle()

        val result = viewModel.messages.value
        result.size shouldBe 2
        result[0].id shouldBe "1"
        result[0].title shouldBe "Title1"
        result[0].lead shouldBe "Lead1"
        result[1].id shouldBe "2"
        result[1].title shouldBe "Title2"
        result[1].lead shouldBe "Lead2"
    }

    @Test
    fun messages_shouldBeEmpty_when_fetchMessagesThrowsException() = runTest(testDispatcher) {
        everySuspend { mockModel.fetchMessages() } throws RuntimeException("Network error")
        val viewModel = createViewModel()

        viewModel.refreshMessages()
        advanceUntilIdle()

        val result = viewModel.messages.value
        result shouldBe emptyList()
    }

    @Test
    fun messages_shouldUpdate_when_refreshMessagesIsCalledMultipleTimes() = runTest(testDispatcher) {
        val firstMessages = listOf(createTestMessage(id = "1", title = "First"))
        val secondMessages = listOf(
            createTestMessage(id = "2", title = "Second"),
            createTestMessage(id = "3", title = "Third")
        )
        everySuspend { mockModel.fetchMessages() } returns firstMessages
        val viewModel = createViewModel()

        viewModel.refreshMessages()
        advanceUntilIdle()
        val firstResult = viewModel.messages.value
        firstResult.size shouldBe 1
        firstResult[0].id shouldBe "1"

        everySuspend { mockModel.fetchMessages() } returns secondMessages
        viewModel.refreshMessages()
        advanceUntilIdle()
        val secondResult = viewModel.messages.value
        secondResult.size shouldBe 2
        secondResult[0].id shouldBe "2"
        secondResult[1].id shouldBe "3"
    }

    @Test
    fun messages_shouldCreateMessageItemViewModels_withCorrectProperties() = runTest(testDispatcher) {
        val testMessage = createTestMessage(
            id = "testId",
            title = "Test Title",
            lead = "Test Lead",
            imageUrl = "https://example.com/image.jpg",
            receivedAt = 1234567890L
        )
        everySuspend { mockModel.fetchMessages() } returns listOf(testMessage)
        val viewModel = createViewModel()

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
    fun isRefreshing_shouldBeFalse_whenViewModelIsCreated() = runTest(testDispatcher) {
        val viewModel = createViewModel()

        val result = viewModel.isRefreshing.value

        result shouldBe false
    }

    @Test
    fun isRefreshing_shouldBeTrue_whenRefreshStarts() = runTest(testDispatcher) {
        everySuspend { mockModel.fetchMessages() } returns emptyList()
        val viewModel = createViewModel()

        viewModel.refreshMessages()

        val result = viewModel.isRefreshing.value
        result shouldBe true
    }

    @Test
    fun isRefreshing_shouldBeFalse_whenRefreshCompletes() = runTest(testDispatcher) {
        everySuspend { mockModel.fetchMessages() } returns emptyList()
        val viewModel = createViewModel()

        viewModel.refreshMessages()

        advanceUntilIdle()

        val result = viewModel.isRefreshing.value
        result shouldBe false
    }

    @Test
    fun isRefreshing_shouldBeFalse_whenRefreshFails() = runTest(testDispatcher) {
        everySuspend { mockModel.fetchMessages() } throws RuntimeException("Network error")
        val viewModel = createViewModel()

        viewModel.refreshMessages()

        advanceUntilIdle()

        val result = viewModel.isRefreshing.value
        result shouldBe false
    }

    @Test
    fun refreshMessages_shouldUpdateIsRefreshingState() = runTest(testDispatcher) {
        val testMessages = listOf(createTestMessage(id = "1", title = "Title1"))
        everySuspend { mockModel.fetchMessages() } returns testMessages
        val viewModel = createViewModel()

        viewModel.isRefreshing.value shouldBe false

        viewModel.refreshMessages()
        viewModel.isRefreshing.value shouldBe true

        advanceUntilIdle()

        viewModel.isRefreshing.value shouldBe false
    }

    private fun createViewModel(): ListPageViewModel = ListPageViewModel(
        mockModel,
        mockDownloaderApi,
        mockSdkEventDistributor,
        CoroutineScope(SupervisorJob() + testDispatcher)
    )

    private fun createTestMessage(
        id: String = "1",
        title: String = "testTitle",
        lead: String = "testLead",
        imageUrl: String? = null,
        receivedAt: Long = 100000L
    ): EmbeddedMessage {
        return EmbeddedMessage(
            id = id,
            title = title,
            lead = lead,
            imageUrl = imageUrl,
            defaultAction = null,
            actions = emptyList<PresentableActionModel>(),
            tags = emptyList(),
            categoryIds = emptyList(),
            receivedAt = receivedAt,
            expiresAt = 110000L,
            properties = emptyMap(),
            trackingInfo = "anything"
        )
    }
}

