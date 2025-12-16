package com.emarsys.mobileengage.embeddedmessaging.ui.list

import androidx.paging.PagingData
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.providers.InstantProvider
import com.emarsys.core.util.DownloaderApi
import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.embeddedmessaging.EmbeddedMessagingContextApi
import com.emarsys.networking.clients.embedded.messaging.model.MessageCategory
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.answering.sequentiallyReturns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
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
    private companion object {
        val CATEGORIES = listOf(
            MessageCategory(id = 1, value = "Category 1"),
            MessageCategory(id = 2, value = "Category 2")
        )
    }

    private lateinit var mockModel: ListPageModelApi
    private lateinit var mockDownloaderApi: DownloaderApi
    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi
    private lateinit var mockActionFactory: ActionFactoryApi<ActionModel>
    private lateinit var viewModel: ListPageViewModel
    private lateinit var mockEmbeddedMessagingContext: EmbeddedMessagingContextApi
    private lateinit var mockPagerFactory: PagerFactoryApi
    private lateinit var mockTimestampProvider: InstantProvider
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockModel = mock(MockMode.autofill)
        mockDownloaderApi = mock(MockMode.autofill)
        mockSdkEventDistributor = mock(MockMode.autofill)
        mockActionFactory = mock(MockMode.autofill)
        mockPagerFactory = mock(MockMode.autofill)
        mockTimestampProvider = mock(MockMode.autofill)
        mockEmbeddedMessagingContext = mock(MockMode.autofill)

        viewModel = ListPageViewModel(
            model = mockModel,
            downloaderApi = mockDownloaderApi,
            sdkEventDistributor = mockSdkEventDistributor,
            actionFactory = mockActionFactory,
            embeddedMessagingContext = mockEmbeddedMessagingContext,
            timestampProvider = mockTimestampProvider,
            coroutineScope = CoroutineScope(SupervisorJob() + testDispatcher),
            pagerFactory = mockPagerFactory
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testSetFilterUnreadOnly_shouldCallPagerFactory_withFilterUnreadOnlyTrue() = runTest {
        viewModel.filterUnreadOnly.value shouldBe false
        viewModel.selectedCategoryIds.value shouldBe emptySet()
        viewModel.categories.value shouldBe emptyList()

        every { mockPagerFactory.create(any(), any(), any()) } returns flowOf(
            PagingData.from(
                data = emptyList(),
                placeholdersBefore = 0,
                placeholdersAfter = 0
            )
        )

        viewModel.setFilterUnreadOnly(true)

        val firstMessage = viewModel.messagePagingDataFlow.first()

        viewModel.filterUnreadOnly.value shouldBe true
        firstMessage shouldNotBe null
        verify {
            mockPagerFactory.create(
                filterUnreadOnly = true,
                selectedCategoryIds = viewModel.selectedCategoryIds.value.toList(),
                categories = any()
            )
        }
    }

    @Test
    fun testSetSelectCategoryIds_shouldCallPagerFactory_withCorrectSelectedCategoryIds() = runTest {
        viewModel.filterUnreadOnly.value shouldBe false
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

        val firstMessage = viewModel.messagePagingDataFlow.first()

        viewModel.selectedCategoryIds.value shouldBe selectedCategoryIds

        firstMessage shouldNotBe null
        verify {
            mockPagerFactory.create(
                filterUnreadOnly = false,
                selectedCategoryIds = selectedCategoryIds.toList(),
                categories = any()
            )
        }
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun testRefreshMessages_shouldApplyThrottling_whenRequestIsTooFrequent() = runTest {
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

        viewModel.refreshMessages(canCallRefresh)
        count shouldBe 1

        viewModel.refreshMessages(canCallRefresh)
        count shouldBe 1

        viewModel.refreshMessages(canCallRefresh)
        count shouldBe 2
    }
}

