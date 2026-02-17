package com.sap.ec.mobileengage.embeddedmessaging.ui.list

import androidx.paging.PagingData
import com.sap.ec.mobileengage.embeddedmessaging.ui.item.MessageItemViewModelApi
import com.sap.ec.networking.clients.embedded.messaging.model.MessageCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal interface PagerFactoryApi {
    fun create(
        filterUnopenedOnly: Boolean,
        selectedCategoryIds: List<Int>,
        categories: MutableStateFlow<List<MessageCategory>>
    ): Flow<PagingData<MessageItemViewModelApi>>
}