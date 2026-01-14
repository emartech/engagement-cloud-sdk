package com.emarsys.mobileengage.embeddedmessaging.pagination

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.log.Logger
import com.emarsys.core.util.DownloaderApi
import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.embeddedmessaging.ui.item.MessageItemViewModelApi
import com.emarsys.mobileengage.embeddedmessaging.ui.list.ListPageModelApi
import com.emarsys.mobileengage.embeddedmessaging.ui.list.MessagesWithCategories
import com.emarsys.networking.clients.embedded.messaging.model.EmbeddedMessage
import com.emarsys.networking.clients.embedded.messaging.model.ListThumbnailImage
import com.emarsys.networking.clients.embedded.messaging.model.MessageCategory
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class EmbeddedMessagingPagingSourceTests {

    private lateinit var mockListPageModel: ListPageModelApi
    private lateinit var mockDownloader: DownloaderApi
    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi

    private lateinit var mockActionFactory: ActionFactoryApi<ActionModel>
    private lateinit var mockLogger: Logger

    @BeforeTest
    fun setup() {
        mockListPageModel = mock()
        mockDownloader = mock()
        mockSdkEventDistributor = mock()
        mockActionFactory = mock()
        mockLogger = mock()

        everySuspend { mockLogger.trace(any()) } returns Unit
        everySuspend { mockLogger.error(any(), any<Throwable>()) } returns Unit

    }

    private fun createEmbeddedMessagingPagingSource(
        filterUnreadOnly: Boolean = false,
        selectedCategoryIds: List<Int> = emptyList(),
        deletedMessageIds: Set<String> = emptySet(),
        setCategories: (List<MessageCategory>) -> Unit = { }
    ) = EmbeddedMessagingPagingSource(
        listPageModel = mockListPageModel,
        filterUnreadOnly = filterUnreadOnly,
        selectedCategoryIds = selectedCategoryIds,
        deletedMessageIds = deletedMessageIds,
        setCategories = setCategories,
        downloader = mockDownloader,
        actionFactory = mockActionFactory,
        sdkEventDistributor = mockSdkEventDistributor,
        logger = mockLogger,
    )

    @Test
    fun testLoad_should_call_fetchMessagesWithCategories_when_invokedFirstTime() = runTest {
        val embeddedMessagingPagingSource = createEmbeddedMessagingPagingSource()
        everySuspend {
            mockListPageModel.fetchMessagesWithCategories(
                false, emptyList()
            )
        } returns Result.success(
            MessagesWithCategories(
                messages = emptyList(), categories = emptyList(), isEndReached = false
            )
        )
        val result: PagingSource.LoadResult<Int, MessageItemViewModelApi> =
            embeddedMessagingPagingSource.load(
                PagingSource.LoadParams.Refresh(
                    key = null, loadSize = 20, placeholdersEnabled = false
                )
            )

        verifySuspend {
            mockListPageModel.fetchMessagesWithCategories(
                filterUnreadOnly = false, categoryIds = emptyList()
            )
        }
        (result is PagingSource.LoadResult.Error) shouldBe false
    }

    @Test
    fun testLoad_should_exclude_deleted_messages_from_the_returned_flow() = runTest {
        val testMessage1 = EmbeddedMessage(
            id = "1",
            title = "testTitle",
            lead = "testLead",
            listThumbnailImage = ListThumbnailImage("example.com", null),
            defaultAction = null,
            actions = emptyList(),
            tags = listOf("deleted"),
            categoryIds = emptyList(),
            receivedAt = 100000L,
            expiresAt = 110000L,
            properties = emptyMap(),
            trackingInfo = "anything"
        )
        val testMessage2 = EmbeddedMessage(
            id = "2",
            title = "testTitle",
            lead = "testLead",
            listThumbnailImage = ListThumbnailImage("example.com", null),
            defaultAction = null,
            actions = emptyList(),
            tags = listOf("not-deleted"),
            categoryIds = emptyList(),
            receivedAt = 100000L,
            expiresAt = 110000L,
            properties = emptyMap(),
            trackingInfo = "anything"
        )
        val testEmbeddedMessagesResponse = listOf(testMessage1, testMessage2)
        val embeddedMessagingPagingSource = createEmbeddedMessagingPagingSource()
        everySuspend {
            mockListPageModel.fetchMessagesWithCategories(
                false, emptyList()
            )
        } returns Result.success(
            MessagesWithCategories(
                messages = testEmbeddedMessagesResponse, categories = emptyList(), isEndReached = false
            )
        )
        val result: PagingSource.LoadResult<Int, MessageItemViewModelApi> =
            embeddedMessagingPagingSource.load(
                PagingSource.LoadParams.Refresh(
                    key = null, loadSize = 20, placeholdersEnabled = false
                )
            )

        (result is PagingSource.LoadResult.Error) shouldBe false
        (result as PagingSource.LoadResult.Page).data.size shouldBe 1
        result.data[0].id shouldBe "2"
    }

    @Test
    fun testLoad_should_exclude_locally_deleted_messages_from_the_returned_flow() = runTest {
        val testMessage1 = EmbeddedMessage(
            id = "1",
            title = "testTitle",
            lead = "testLead",
            listThumbnailImage = ListThumbnailImage("example.com", null),
            defaultAction = null,
            actions = emptyList(),
            tags = listOf(),
            categoryIds = emptyList(),
            receivedAt = 100000L,
            expiresAt = 110000L,
            properties = emptyMap(),
            trackingInfo = "anything"
        )
        val testMessage2 = EmbeddedMessage(
            id = "2",
            title = "testTitle",
            lead = "testLead",
            listThumbnailImage = ListThumbnailImage("example.com", null),
            defaultAction = null,
            actions = emptyList(),
            tags = listOf(),
            categoryIds = emptyList(),
            receivedAt = 100000L,
            expiresAt = 110000L,
            properties = emptyMap(),
            trackingInfo = "anything"
        )
        val testEmbeddedMessagesResponse = listOf(testMessage1, testMessage2)
        val embeddedMessagingPagingSource = createEmbeddedMessagingPagingSource(deletedMessageIds = setOf("2"))
        everySuspend {
            mockListPageModel.fetchMessagesWithCategories(
                false, emptyList()
            )
        } returns Result.success(
            MessagesWithCategories(
                messages = testEmbeddedMessagesResponse, categories = emptyList(), isEndReached = false
            )
        )
        val result: PagingSource.LoadResult<Int, MessageItemViewModelApi> =
            embeddedMessagingPagingSource.load(
                PagingSource.LoadParams.Refresh(
                    key = null, loadSize = 20, placeholdersEnabled = false
                )
            )

        (result is PagingSource.LoadResult.Error) shouldBe false
        (result as PagingSource.LoadResult.Page).data.size shouldBe 1
        result.data[0].id shouldBe "1"
    }

    @Test
    fun testLoad_should_call_fetchMessagesWithCategories_when_invokedFirstTime_and_HandleErrors_WhenResultFailed() =
        runTest {
            val embeddedMessagingPagingSource = createEmbeddedMessagingPagingSource()
            everySuspend {
                mockListPageModel.fetchMessagesWithCategories(
                    false, emptyList()
                )
            } returns Result.failure(
                Exception("Test Exception")
            )
            val result: PagingSource.LoadResult<Int, MessageItemViewModelApi> =
                embeddedMessagingPagingSource.load(
                    PagingSource.LoadParams.Refresh(
                        key = null, loadSize = 20, placeholdersEnabled = false
                    )
                )

            verifySuspend {
                mockListPageModel.fetchMessagesWithCategories(
                    filterUnreadOnly = false, categoryIds = emptyList()
                )
            }
            (result is PagingSource.LoadResult.Error) shouldBe true
        }

    @Test
    fun testLoad_should_catch_Exception_onFailure() = runTest {
        val embeddedMessagingPagingSource = createEmbeddedMessagingPagingSource()
        everySuspend {
            mockListPageModel.fetchMessagesWithCategories(
                false, emptyList()
            )
        } throws Exception("Test Exception")

        val result: PagingSource.LoadResult<Int, MessageItemViewModelApi> =
            embeddedMessagingPagingSource.load(
                PagingSource.LoadParams.Refresh(
                    key = null, loadSize = 20, placeholdersEnabled = false
                )
            )

        (result is PagingSource.LoadResult.Error) shouldBe true
    }

    @Test
    fun testLoad_should_call_fetchNextPage_when_notOnFirstPage_andHandleError_whenResultFailed() =
        runTest {
            val embeddedMessagingPagingSource = createEmbeddedMessagingPagingSource()
            everySuspend {
                mockListPageModel.fetchNextPage()
            } returns Result.failure(
                Exception("Test Exception")
            )
            val result: PagingSource.LoadResult<Int, MessageItemViewModelApi> =
                embeddedMessagingPagingSource.load(
                    PagingSource.LoadParams.Refresh(
                        key = 1, loadSize = 20, placeholdersEnabled = false
                    )
                )

            verifySuspend {
                mockListPageModel.fetchNextPage()
            }
            (result is PagingSource.LoadResult.Error) shouldBe true
        }

    @Test
    fun testLoad_should_call_fetchNextPage_when_notOnFirstPage() = runTest {
        val embeddedMessagingPagingSource = createEmbeddedMessagingPagingSource()
        everySuspend {
            mockListPageModel.fetchNextPage()
        } returns Result.success(
            MessagesWithCategories(
                messages = emptyList(), categories = emptyList(), isEndReached = false
            )
        )
        val result: PagingSource.LoadResult<Int, MessageItemViewModelApi> =
            embeddedMessagingPagingSource.load(
                PagingSource.LoadParams.Refresh(
                    key = 1, loadSize = 20, placeholdersEnabled = false
                )
            )

        verifySuspend {
            mockListPageModel.fetchNextPage()
        }
        (result is PagingSource.LoadResult.Error) shouldBe false
    }

    @Test
    fun testGetRefreshKey_should_return_0() = runTest {
        val embeddedMessagingPagingSource = createEmbeddedMessagingPagingSource()

        val result = embeddedMessagingPagingSource.getRefreshKey(PagingState(
            pages = emptyList(),
            anchorPosition = null,
            config = PagingConfig(pageSize = 20),
            leadingPlaceholderCount = 0
        ))

        result shouldBe 0
    }

}