package com.sap.ec.mobileengage.embeddedmessaging.ui.list

import androidx.paging.PagingData
import com.sap.ec.mobileengage.embeddedmessaging.ui.item.MessageItemViewModelApi
import com.sap.ec.networking.clients.embedded.messaging.model.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

internal interface ListPageViewModelApi {
    val messagePagingDataFlowFiltered: Flow<PagingData<MessageItemViewModelApi>>
    val categories: StateFlow<List<Category>>

    val filterUnopenedOnly: StateFlow<Boolean>
    val selectedCategoryIds: StateFlow<Set<String>>
    val hasFiltersApplied: StateFlow<Boolean>
    val hasConnection: StateFlow<Boolean>

    val selectedMessage: StateFlow<MessageItemViewModelApi?>

    val showCategorySelector: StateFlow<Boolean>

    val triggerRefreshFromJs: () -> Unit

    val platformCategory: String

    val hasTouchInput: Boolean

    fun setFilterUnopenedOnly(unopenedOnly: Boolean)
    fun setSelectedCategoryIds(categoryIds: Set<String>)

    fun selectMessage(messageViewModel: MessageItemViewModelApi, onNavigate: suspend () -> Unit)
    suspend fun deleteMessage(messageViewModel: MessageItemViewModelApi): Result<Unit>
    suspend fun tagMessageRead(messageViewModel: MessageItemViewModelApi): Result<Unit>

    fun clearMessageSelection()

    fun openCategorySelector()
    fun closeCategorySelector()
    fun applyCategorySelection(categoryIds: Set<String>)

    fun refreshMessagesWithThrottling(shouldCallRefresh: () -> Unit)
}