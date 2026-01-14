package com.emarsys.mobileengage.embeddedmessaging.ui.list

import androidx.paging.PagingData
import com.emarsys.mobileengage.embeddedmessaging.ui.item.MessageItemViewModelApi
import com.emarsys.networking.clients.embedded.messaging.model.MessageCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal interface PagerFactoryApi {
    fun create(
        filterUnreadOnly: Boolean,
        selectedCategoryIds: List<Int>,
        deletedMessageIds: Set<String>,
        categories: MutableStateFlow<List<MessageCategory>>
    ): Flow<PagingData<MessageItemViewModelApi>>
}