package com.emarsys.mobileengage.embeddedmessaging.pagination

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.log.Logger
import com.emarsys.core.util.DownloaderApi
import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.embeddedmessaging.ui.item.MessageItemModel
import com.emarsys.mobileengage.embeddedmessaging.ui.item.MessageItemViewModel
import com.emarsys.mobileengage.embeddedmessaging.ui.item.MessageItemViewModelApi
import com.emarsys.mobileengage.embeddedmessaging.ui.list.ListPageModelApi
import com.emarsys.networking.clients.embedded.messaging.model.MessageCategory

internal class EmbeddedMessagingPagingSource(
    private val listPageModel: ListPageModelApi,
    private val filterUnreadOnly: Boolean,
    private val selectedCategoryIds: List<Int>,
    private val setCategories: (List<MessageCategory>) -> Unit,
    private val downloader: DownloaderApi,
    private val actionFactory: ActionFactoryApi<ActionModel>,
    private val sdkEventDistributor: SdkEventDistributorApi,
    private val logger: Logger
) : PagingSource<Int, MessageItemViewModelApi>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MessageItemViewModelApi> {
        return try {
            val pageNumber = params.key ?: 0
            logger.trace("pageNumber: $pageNumber")
            if (pageNumber == 0) {
                listPageModel.fetchMessagesWithCategories(
                    filterUnreadOnly = filterUnreadOnly,
                    categoryIds = selectedCategoryIds,
                )
            } else {
                listPageModel.fetchNextPage()
            }.fold(
                onSuccess = {
                    setCategories(it.categories)
                    val prevKey = if (pageNumber > 0) pageNumber - 1 else null
                    val nextKey =
                        if (it.isEndReached) null else pageNumber + 1
                    LoadResult.Page(
                        it.messages.map { message ->
                            MessageItemViewModel(
                                MessageItemModel(
                                    message,
                                    downloader,
                                    sdkEventDistributor,
                                    actionFactory,
                                    logger
                                )
                            )
                        },
                        prevKey,
                        nextKey
                    )
                },
                onFailure = {
                    LoadResult.Error(it)
                })
        } catch (e: Exception) {
            logger.error("Error loading page", e)
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, MessageItemViewModelApi>): Int? {
        return 0
    }
}