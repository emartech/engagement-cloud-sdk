package com.emarsys.mobileengage.embeddedmessaging.ui.list

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.util.DownloaderApi
import com.emarsys.mobileengage.embeddedmessaging.ui.item.MessageItemModel
import com.emarsys.mobileengage.embeddedmessaging.ui.item.MessageItemViewModel
import com.emarsys.mobileengage.embeddedmessaging.ui.item.MessageItemViewModelApi
import com.emarsys.networking.clients.embedded.messaging.model.MessageCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class ListPageViewModel(
    private val model: ListPageModelApi,
    private val downloaderApi: DownloaderApi,
    private val sdkEventDistributor: SdkEventDistributorApi,
    private val coroutineScope: CoroutineScope,
) : ListPageViewModelApi {
    private val _messages = MutableStateFlow<List<MessageItemViewModelApi>>(emptyList())
    override val messages: StateFlow<List<MessageItemViewModelApi>> = _messages.asStateFlow()
    private val _categories = MutableStateFlow<List<MessageCategory>>(emptyList())
    override val categories: StateFlow<List<MessageCategory>> = _categories.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    override val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _filterUnreadOnly = MutableStateFlow(false)
    override val filterUnreadOnly: StateFlow<Boolean> = _filterUnreadOnly.asStateFlow()
    private val _selectedCategoryIds = MutableStateFlow<Set<Int>>(emptySet())
    override val selectedCategoryIds: StateFlow<Set<Int>> = _selectedCategoryIds.asStateFlow()

    override fun refreshMessages() {
        _isRefreshing.value = true
        loadMessages()
    }

    override fun setFilterUnreadOnly(unreadOnly: Boolean) {
        _filterUnreadOnly.value = unreadOnly
        refreshMessages()
    }

    override fun setSelectedCategoryIds(categoryIds: Set<Int>) {
        _selectedCategoryIds.value = categoryIds
    }

    private fun loadMessages() {
        coroutineScope.launch {
            val fetchResult = model.fetchMessagesWithCategories(
                filterUnreadOnly = _filterUnreadOnly.value,
                categoryIds = _selectedCategoryIds.value.toList()
            )

            fetchResult
                .onSuccess {
                    val messageViewModels = it.messages.map { message ->
                        MessageItemViewModel(
                            MessageItemModel(
                                message,
                                downloaderApi,
                                sdkEventDistributor
                            )
                        )
                    }
                    _messages.value = messageViewModels
                    _categories.value = it.categories
                    _isRefreshing.value = false
                }
                .onFailure {
                    _isRefreshing.value = false
                }
        }
    }
}