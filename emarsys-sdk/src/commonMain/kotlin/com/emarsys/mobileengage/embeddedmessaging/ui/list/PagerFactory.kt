package com.emarsys.mobileengage.embeddedmessaging.ui.list

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.log.Logger
import com.emarsys.core.util.DownloaderApi
import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.embeddedmessaging.pagination.EmbeddedMessagingPagingSource
import com.emarsys.mobileengage.embeddedmessaging.ui.item.MessageItemViewModelApi
import com.emarsys.networking.clients.embedded.messaging.model.MessageCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class PagerFactory(
    private val model: ListPageModelApi,
    private val downloaderApi: DownloaderApi,
    private val sdkEventDistributor: SdkEventDistributorApi,
    private val actionFactory: ActionFactoryApi<ActionModel>,
    private val logger: Logger
) : PagerFactoryApi {
    override fun create(
        filterUnopenedOnly: Boolean,
        selectedCategoryIds: List<Int>,
        categories: MutableStateFlow<List<MessageCategory>>
    ): Flow<PagingData<MessageItemViewModelApi>> {
        return Pager(
            config = PagingConfig(
                pageSize = -1,  // ignored because it is set by the backend
                prefetchDistance = 2,
                enablePlaceholders = true,
                initialLoadSize = -1,  // also ignored
                maxSize = Int.MAX_VALUE,
            ),
            initialKey = 0,
            pagingSourceFactory = {
                EmbeddedMessagingPagingSource(
                    listPageModel = model,
                    filterUnopenedOnly = filterUnopenedOnly,
                    selectedCategoryIds = selectedCategoryIds.toList(),
                    setCategories = { categories.value = it },
                    downloader = downloaderApi,
                    actionFactory = actionFactory,
                    sdkEventDistributor = sdkEventDistributor,
                    logger = logger
                )
            }
        ).flow
    }

}