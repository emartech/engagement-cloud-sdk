package com.emarsys.mobileengage.embeddedmessaging.ui.list

import androidx.paging.PagingData
import com.emarsys.mobileengage.embeddedmessaging.ui.item.MessageItemViewModelApi
import com.emarsys.networking.clients.embedded.messaging.model.MessageCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ListPageViewModelApi {
    val messagePagingDataFlowFiltered: Flow<PagingData<MessageItemViewModelApi>>
    val categories: StateFlow<List<MessageCategory>>

    val filterUnreadOnly: StateFlow<Boolean>
    val selectedCategoryIds: StateFlow<Set<Int>>
    val hasFiltersApplied: StateFlow<Boolean>
    val hasConnection: StateFlow<Boolean>

    val selectedMessage: StateFlow<MessageItemViewModelApi?>

    val showCategorySelector: StateFlow<Boolean>

    fun setFilterUnreadOnly(unreadOnly: Boolean)
    fun setSelectedCategoryIds(categoryIds: Set<Int>)

    suspend fun selectMessage(messageViewModel: MessageItemViewModelApi, onNavigate: suspend () -> Unit)
    suspend fun deleteMessage(messageViewModel: MessageItemViewModelApi): Result<Unit>

    fun clearMessageSelection()

    fun openCategorySelector()
    fun closeCategorySelector()
    fun applyCategorySelection(categoryIds: Set<Int>)

    fun refreshMessagesWithThrottling(shouldCallRefresh: () -> Unit)

    val triggerRefreshFromJs: () -> Unit
}