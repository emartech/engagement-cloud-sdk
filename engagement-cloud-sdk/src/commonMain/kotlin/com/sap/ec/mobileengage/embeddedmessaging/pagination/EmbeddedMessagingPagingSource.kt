package com.sap.ec.mobileengage.embeddedmessaging.pagination

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.util.DownloaderApi
import com.sap.ec.mobileengage.action.ActionFactoryApi
import com.sap.ec.mobileengage.action.models.ActionModel
import com.sap.ec.mobileengage.embeddedmessaging.exceptions.LastPageReachedException
import com.sap.ec.mobileengage.embeddedmessaging.ui.item.MessageItemModel
import com.sap.ec.mobileengage.embeddedmessaging.ui.item.MessageItemViewModel
import com.sap.ec.mobileengage.embeddedmessaging.ui.item.MessageItemViewModelApi
import com.sap.ec.mobileengage.embeddedmessaging.ui.list.ListPageModelApi
import com.sap.ec.networking.clients.embedded.messaging.model.MessageCategory

internal class EmbeddedMessagingPagingSource(
    private val listPageModel: ListPageModelApi,
    private val filterUnopenedOnly: Boolean,
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
                    filterUnopenedOnly = filterUnopenedOnly,
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
                    if (it is LastPageReachedException) {
                        logger.debug("Last page reached")
                        LoadResult.Page(
                            emptyList(),
                            prevKey = if (pageNumber > 0) pageNumber - 1 else null,
                            nextKey = null
                        )
                    } else {
                        LoadResult.Error(it)
                    }
                })
        } catch (e: Exception) {
            logger.error("Error loading page", e)
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, MessageItemViewModelApi>): Int {
        return 0
    }
}