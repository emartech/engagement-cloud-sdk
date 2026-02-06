package com.emarsys.mobileengage.embeddedmessaging.ui.list

import androidx.paging.PagingData
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.providers.InstantProvider
import com.emarsys.core.providers.platform.PlatformCategoryProviderApi
import com.emarsys.core.util.DownloaderApi
import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.embeddedmessaging.EmbeddedMessagingContextApi
import com.emarsys.mobileengage.embeddedmessaging.ui.item.MessageItemViewModelApi
import com.emarsys.watchdog.connection.ConnectionWatchDog
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.answering.sequentiallyReturns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class)
class ListPageViewModelTests {
    companion object {
        const val TEST_MESSAGE_ID = "test-message-id"
        const val PLATFORM_CATEGORY = "testCategory"
    }

    private lateinit var mockModel: ListPageModelApi
    private lateinit var mockDownloaderApi: DownloaderApi
    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi
    private lateinit var mockActionFactory: ActionFactoryApi<ActionModel>
    private lateinit var viewModel: ListPageViewModel
    private lateinit var mockEmbeddedMessagingContext: EmbeddedMessagingContextApi
    private lateinit var mockPagerFactory: PagerFactoryApi
    private lateinit var mockTimestampProvider: InstantProvider
    private lateinit var mockConnectionWatchDog: ConnectionWatchDog
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var deletedMessageIds: MutableStateFlow<Set<String>>
    private lateinit var readMessageIds: MutableStateFlow<Set<String>>
    private lateinit var mockPlatformCategoryProvider: PlatformCategoryProviderApi

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockModel = mock(MockMode.autofill)
        mockDownloaderApi = mock(MockMode.autofill)
        mockSdkEventDistributor = mock(MockMode.autofill)
        mockActionFactory = mock(MockMode.autofill)
        mockPagerFactory = mock(MockMode.autofill)
        mockTimestampProvider = mock(MockMode.autofill)
        mockConnectionWatchDog = mock(MockMode.autofill)
        mockEmbeddedMessagingContext = mock(MockMode.autofill)
        mockPlatformCategoryProvider = mock(MockMode.autofill)
        every { mockPlatformCategoryProvider.provide() } returns PLATFORM_CATEGORY
        deletedMessageIds = MutableStateFlow(emptySet())
        readMessageIds = MutableStateFlow(emptySet())

        viewModel = ListPageViewModel(
            embeddedMessagingContext = mockEmbeddedMessagingContext,
            timestampProvider = mockTimestampProvider,
            coroutineScope = CoroutineScope(SupervisorJob() + testDispatcher),
            pagerFactory = mockPagerFactory,
            connectionWatchDog = mockConnectionWatchDog,
            locallyDeletedMessageIds = deletedMessageIds,
            locallyOpenedMessageIds = readMessageIds,
            mockPlatformCategoryProvider
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testSetFilterUnopenedOnly_shouldCallPagerFactory_withFilterUnopenedOnlyTrue() = runTest {
        viewModel.filterUnopenedOnly.value shouldBe false
        viewModel.selectedCategoryIds.value shouldBe emptySet()
        viewModel.categories.value shouldBe emptyList()

        every { mockPagerFactory.create(any(), any(), any()) } returns flowOf(
            PagingData.from(
                data = emptyList(),
                placeholdersBefore = 0,
                placeholdersAfter = 0
            )
        )

        viewModel.setFilterUnopenedOnly(true)

        val firstMessage = viewModel.messagePagingDataFlowFiltered.first()

        viewModel.filterUnopenedOnly.value shouldBe true
        firstMessage shouldNotBe null
        verify {
            mockPagerFactory.create(
                filterUnopenedOnly = true,
                selectedCategoryIds = viewModel.selectedCategoryIds.value.toList(),
                categories = any()
            )
        }
    }

    @Test
    fun testSetSelectCategoryIds_shouldCallPagerFactory_withCorrectSelectedCategoryIds() = runTest {
        viewModel.filterUnopenedOnly.value shouldBe false
        viewModel.selectedCategoryIds.value shouldBe emptySet()
        viewModel.categories.value shouldBe emptyList()

        val selectedCategoryIds = setOf(1, 2, 3)

        every { mockPagerFactory.create(any(), any(), any()) } returns flowOf(
            PagingData.from(
                data = emptyList(),
                placeholdersBefore = 0,
                placeholdersAfter = 0
            )
        )

        viewModel.setSelectedCategoryIds(selectedCategoryIds)

        val firstMessage = viewModel.messagePagingDataFlowFiltered.first()

        viewModel.selectedCategoryIds.value shouldBe selectedCategoryIds

        firstMessage shouldNotBe null
        verify {
            mockPagerFactory.create(
                filterUnopenedOnly = false,
                selectedCategoryIds = selectedCategoryIds.toList(),
                categories = any()
            )
        }
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun testRefreshMessages_WithThrottling_shouldApplyThrottling_whenRequestIsTooFrequent() =
        runTest {
            var count = 0
            val canCallRefresh: () -> Unit = { count++ }

            val firstRequest = Clock.System.now()
            val secondRequestThatShouldBeThrottled = firstRequest.plus(2.seconds)
            val thirdRequest = firstRequest.plus(12.seconds)
            val fourthRequest = firstRequest.plus(25.seconds)

            every { mockTimestampProvider.provide() } sequentiallyReturns listOf(
                firstRequest,
                secondRequestThatShouldBeThrottled,
                thirdRequest,
                fourthRequest
            )
            every { mockEmbeddedMessagingContext.embeddedMessagingFrequencyCapSeconds } returns 10

            viewModel.refreshMessagesWithThrottling(canCallRefresh)
            count shouldBe 1

            viewModel.refreshMessagesWithThrottling(canCallRefresh)
            count shouldBe 1

            viewModel.refreshMessagesWithThrottling(canCallRefresh)
            count shouldBe 2
        }

    @Test
    fun testSelectMessage_shouldUpdateSelectedMessageAndCache_withoutNavigation_andHandleDefaultAction() =
        runTest {
            val mockMessageViewModel = mock<MessageItemViewModelApi>(MockMode.autofill)
            every { mockMessageViewModel.id } returns TEST_MESSAGE_ID
            every { mockMessageViewModel.isNotOpened } returns true
            every { mockMessageViewModel.shouldNavigate() } returns false
            everySuspend { mockMessageViewModel.tagMessageOpened() } returns Result.success(Unit)

            viewModel.selectedMessage.value shouldBe null

            var navigationCalled = false
            viewModel.selectMessage(mockMessageViewModel) {
                navigationCalled = true
            }

            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.selectedMessage.value shouldBe mockMessageViewModel
            navigationCalled shouldBe false
            readMessageIds.value shouldBe setOf(TEST_MESSAGE_ID)
            verifySuspend {
                mockMessageViewModel.handleDefaultAction()
            }
        }

    @Test
    fun testSelectMessage_shouldCallNavigationCallback_whenMessageShouldNavigateToDetailView_andNotHandleDefaultAction() =
        runTest {
            val mockMessageViewModel = mock<MessageItemViewModelApi>(MockMode.autofill)
            every { mockMessageViewModel.id } returns TEST_MESSAGE_ID
            every { mockMessageViewModel.isNotOpened } returns true
            every { mockMessageViewModel.shouldNavigate() } returns true
            everySuspend { mockMessageViewModel.tagMessageOpened() } returns Result.success(Unit)

            var navigationCalled = false
            viewModel.selectMessage(mockMessageViewModel) {
                navigationCalled = true
            }

            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.selectedMessage.value shouldBe mockMessageViewModel
            navigationCalled shouldBe true
            readMessageIds.value shouldBe setOf(TEST_MESSAGE_ID)
            verifySuspend(VerifyMode.exactly(0)) {
                mockMessageViewModel.handleDefaultAction()
            }
        }

    @Test
    fun testSelectMessage_shouldCall_tagMessageRead_ifTheMessage_isNotOpened_andNotInLocallyOpened() =
        runTest {
            val mockMessageViewModel = mock<MessageItemViewModelApi>(MockMode.autofill)
            every { mockMessageViewModel.id } returns TEST_MESSAGE_ID
            every { mockMessageViewModel.isNotOpened } returns true
            everySuspend { mockMessageViewModel.tagMessageOpened() } returns Result.success(Unit)

            viewModel.selectMessage(mockMessageViewModel) {}

            testDispatcher.scheduler.advanceUntilIdle()

            verifySuspend { mockMessageViewModel.tagMessageOpened() }
        }

    @Test
    fun testSelectMessage_shouldNotCall_tagMessageRead_ifTheMessage_read() =
        runTest {
            val mockMessageViewModel = mock<MessageItemViewModelApi>(MockMode.autofill)
            every { mockMessageViewModel.id } returns TEST_MESSAGE_ID
            every { mockMessageViewModel.isNotOpened } returns false
            everySuspend { mockMessageViewModel.tagMessageOpened() } returns Result.success(Unit)

            viewModel.selectMessage(mockMessageViewModel) {}
            testDispatcher.scheduler.advanceUntilIdle()

            verifySuspend(VerifyMode.exactly(0)) { mockMessageViewModel.tagMessageOpened() }
        }

    @Test
    fun testSelectMessage_shouldNotCall_tagMessageRead_ifTheMessage_isNotOpened_butIncludedInLocallyOpened() =
        runTest {
            val mockMessageViewModel = mock<MessageItemViewModelApi>(MockMode.autofill)
            every { mockMessageViewModel.id } returns TEST_MESSAGE_ID
            every { mockMessageViewModel.isNotOpened } returns true
            everySuspend { mockMessageViewModel.tagMessageOpened() } returns Result.success(Unit)

            viewModel.selectMessage(mockMessageViewModel) {}
            testDispatcher.scheduler.advanceUntilIdle()

            readMessageIds.value shouldBe setOf(TEST_MESSAGE_ID)

            viewModel.selectMessage(mockMessageViewModel) {}
            testDispatcher.scheduler.advanceUntilIdle()

            verifySuspend(VerifyMode.exactly(1)) { mockMessageViewModel.tagMessageOpened() }
        }

    @Test
    fun testDeleteMessage_shouldCallMessageViewModel_deleteMessage_andUpdateDeletedMessageIdsFilter() =
        runTest {
            val mockMessageViewModel = mock<MessageItemViewModelApi>(MockMode.autofill)
            every { mockMessageViewModel.id } returns TEST_MESSAGE_ID
            everySuspend { mockMessageViewModel.deleteMessage() } returns Result.success(Unit)

            every { mockPagerFactory.create(any(), any(), any()) } returns flowOf(
                PagingData.from(
                    data = listOf(mockMessageViewModel),
                    placeholdersBefore = 0,
                    placeholdersAfter = 0
                )
            )

            val pagingDataList = mutableListOf<PagingData<MessageItemViewModelApi>>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.messagePagingDataFlowFiltered.collect {
                    pagingDataList.add(it)
                }
            }

            viewModel.deleteMessage(mockMessageViewModel)
            advanceUntilIdle()

            pagingDataList.size shouldBe 1
            verify(VerifyMode.exactly(1)) {
                mockPagerFactory.create(any(), any(), any())
            }
            verifySuspend {
                mockMessageViewModel.deleteMessage()
            }
            deletedMessageIds.value shouldBe setOf(TEST_MESSAGE_ID)
        }

    @Test
    fun testDeleteMessage_shouldClearMessageSelection_whenDeleteSucceeded_andSelectedMessageWasDeleted() =
        runTest {
            val mockMessageViewModel = mock<MessageItemViewModelApi>(MockMode.autofill)
            every { mockMessageViewModel.id } returns TEST_MESSAGE_ID
            everySuspend { mockMessageViewModel.deleteMessage() } returns Result.success(Unit)

            viewModel.selectMessage(mockMessageViewModel) {}
            viewModel.deleteMessage(mockMessageViewModel)
            advanceUntilIdle()


            verifySuspend {
                mockMessageViewModel.deleteMessage()
            }
            deletedMessageIds.value shouldBe setOf(TEST_MESSAGE_ID)
        }

    @Test
    fun testClearSelection_shouldSetSelectedMessageAndCacheToNull() = runTest {
        val mockMessageViewModel = mock<MessageItemViewModelApi>(MockMode.autofill)
        every { mockMessageViewModel.id } returns TEST_MESSAGE_ID
        every { mockMessageViewModel.shouldNavigate() } returns false

        viewModel.selectMessage(mockMessageViewModel) {}
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectedMessage.value shouldNotBe null

        viewModel.clearMessageSelection()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectedMessage.value shouldBe null
    }

    @Test
    fun testOpenCategorySelector_shouldSetShowCategorySelectorToTrue() = runTest {
        viewModel.showCategorySelector.value shouldBe false

        viewModel.openCategorySelector()

        viewModel.showCategorySelector.value shouldBe true
    }

    @Test
    fun testCloseCategorySelector_shouldSetShowCategorySelectorToFalse() = runTest {
        viewModel.openCategorySelector()
        viewModel.showCategorySelector.value shouldBe true

        viewModel.closeCategorySelector()

        viewModel.showCategorySelector.value shouldBe false
    }

    @Test
    fun testApplyCategorySelection_shouldSetCategoryIdsAndCloseDialog() = runTest {
        val selectedCategoryIds = setOf(1, 2, 3)

        every { mockPagerFactory.create(any(), any(), any()) } returns flowOf(
            PagingData.from(
                data = emptyList(),
                placeholdersBefore = 0,
                placeholdersAfter = 0
            )
        )

        viewModel.openCategorySelector()
        viewModel.showCategorySelector.value shouldBe true
        viewModel.selectedCategoryIds.value shouldBe emptySet()

        viewModel.applyCategorySelection(selectedCategoryIds)

        viewModel.selectedCategoryIds.value shouldBe selectedCategoryIds
        viewModel.showCategorySelector.value shouldBe false
    }

    @Test
    fun testHasFiltersApplied_shouldReturnTrue_whenFilterUnopenedOnlyIsTrue() = runTest {
        every { mockPagerFactory.create(any(), any(), any()) } returns flowOf(
            PagingData.from(
                data = emptyList(),
                placeholdersBefore = 0,
                placeholdersAfter = 0
            )
        )

        viewModel.hasFiltersApplied.value shouldBe false

        viewModel.setFilterUnopenedOnly(true)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.hasFiltersApplied.value shouldBe true
    }

    @Test
    fun testHasFiltersApplied_shouldReturnTrue_whenSelectedCategoryIdsIsNotEmpty() = runTest {
        val selectedCategoryIds = setOf(1, 2)

        every { mockPagerFactory.create(any(), any(), any()) } returns flowOf(
            PagingData.from(
                data = emptyList(),
                placeholdersBefore = 0,
                placeholdersAfter = 0
            )
        )

        viewModel.hasFiltersApplied.value shouldBe false

        viewModel.setSelectedCategoryIds(selectedCategoryIds)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.hasFiltersApplied.value shouldBe true
    }

    @Test
    fun testHasFiltersApplied_shouldReturnTrue_whenBothFiltersAreApplied() = runTest {
        val selectedCategoryIds = setOf(3, 4)

        every { mockPagerFactory.create(any(), any(), any()) } returns flowOf(
            PagingData.from(
                data = emptyList(),
                placeholdersBefore = 0,
                placeholdersAfter = 0
            )
        )

        viewModel.hasFiltersApplied.value shouldBe false

        viewModel.setFilterUnopenedOnly(true)
        viewModel.setSelectedCategoryIds(selectedCategoryIds)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.hasFiltersApplied.value shouldBe true
    }

    @Test
    fun testHasFiltersApplied_shouldReturnFalse_whenNoFiltersAreApplied() = runTest {
        viewModel.hasFiltersApplied.value shouldBe false

        viewModel.setFilterUnopenedOnly(false)
        viewModel.setSelectedCategoryIds(emptySet())
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.hasFiltersApplied.value shouldBe false
    }

    @Test
    fun platformCategory_shouldReturn_platformCategory_fromProvider() {
        viewModel.platformCategory shouldBe PLATFORM_CATEGORY
    }
}

