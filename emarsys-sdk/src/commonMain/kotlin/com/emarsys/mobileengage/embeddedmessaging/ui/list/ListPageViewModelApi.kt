package com.emarsys.mobileengage.embeddedmessaging.ui.list

import com.emarsys.mobileengage.embeddedmessaging.ui.item.MessageItemViewModel
import kotlinx.coroutines.flow.StateFlow

interface ListPageViewModelApi {
    val messages: StateFlow<List<MessageItemViewModel>>
    val isRefreshing: StateFlow<Boolean>
    val filterUnreadOnly: StateFlow<Boolean>

    fun refreshMessages()
    fun setFilterUnreadOnly(unreadOnly: Boolean)
}