package com.emarsys.mobileengage.embeddedmessaging.ui.list

import androidx.paging.PagingData
import com.emarsys.mobileengage.embeddedmessaging.ui.item.MessageItemViewModelApi
import com.emarsys.networking.clients.embedded.messaging.model.MessageCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ListPageViewModelApi {
    val messagePagingDataFlow: Flow<PagingData<MessageItemViewModelApi>>
    val categories: StateFlow<List<MessageCategory>>
    val filterUnreadOnly: StateFlow<Boolean>
    val selectedCategoryIds: StateFlow<Set<Int>>

    fun setFilterUnreadOnly(unreadOnly: Boolean)
    fun setSelectedCategoryIds(categoryIds: Set<Int>)

    fun refreshMessages(canCallRefresh : () -> Unit)
}