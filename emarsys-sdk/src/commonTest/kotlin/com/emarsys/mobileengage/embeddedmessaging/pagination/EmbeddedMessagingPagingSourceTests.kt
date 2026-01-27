package com.emarsys.mobileengage.embeddedmessaging.pagination

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.log.Logger
import com.emarsys.core.util.DownloaderApi
import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.embeddedmessaging.exceptions.LastPageReachedException
import com.emarsys.mobileengage.embeddedmessaging.ui.item.MessageItemViewModelApi
import com.emarsys.mobileengage.embeddedmessaging.ui.list.ListPageModelApi
import com.emarsys.mobileengage.embeddedmessaging.ui.list.MessagesWithCategories
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
        filterUnopenedOnly: Boolean = false,
        selectedCategoryIds: List<Int> = emptyList(),
        setCategories: (List<MessageCategory>) -> Unit = { }
    ) = EmbeddedMessagingPagingSource(
        listPageModel = mockListPageModel,
        filterUnopenedOnly = filterUnopenedOnly,
        selectedCategoryIds = selectedCategoryIds,
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
                filterUnopenedOnly = false, categoryIds = emptyList()
            )
        }
        (result is PagingSource.LoadResult.Error) shouldBe false
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
                    filterUnopenedOnly = false, categoryIds = emptyList()
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
    fun testLoad_should_return_EmptyLoadResultPageWithNullNextKeyAndEmptyData_when_LastPageReachedExceptionThrown() =
        runTest {
            val embeddedMessagingPagingSource = createEmbeddedMessagingPagingSource()
            everySuspend {
                mockListPageModel.fetchNextPage()
            } returns Result.failure(LastPageReachedException("Can't fetch more pages because last page reached"))

            val actualPageNumber = 7

            everySuspend { mockLogger.debug(any<String>()) } returns Unit

            val result: PagingSource.LoadResult<Int, MessageItemViewModelApi> =
                embeddedMessagingPagingSource.load(
                    PagingSource.LoadParams.Append(
                        key = actualPageNumber, loadSize = -1, placeholdersEnabled = true
                    )
                )

            verifySuspend {
                mockListPageModel.fetchNextPage()
            }
            result shouldBe PagingSource.LoadResult.Page(
                data = emptyList(),
                prevKey = actualPageNumber - 1,
                nextKey = null
            )
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

        val result = embeddedMessagingPagingSource.getRefreshKey(
            PagingState(
                pages = emptyList(),
                anchorPosition = null,
                config = PagingConfig(pageSize = 20),
                leadingPlaceholderCount = 0
            )
        )

        result shouldBe 0
    }

}