package com.emarsys.mobileengage.embeddedmessaging.ui.list

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.util.DownloaderApi
import com.emarsys.mobileengage.embeddedmessaging.ui.item.MessageItemModel
import com.emarsys.mobileengage.embeddedmessaging.ui.item.MessageItemViewModel
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
    private val _messages = MutableStateFlow<List<MessageItemViewModel>>(emptyList())
    override val messages: StateFlow<List<MessageItemViewModel>> = _messages.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    override val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    override fun refreshMessages() {
        _isRefreshing.value = true
        loadMessages()
    }

    private fun loadMessages() {
        coroutineScope.launch {
            //TODO: move error handling and fallback image providing inside model
            try {
                val messageViewModels = model.fetchMessages().map { message ->
                    MessageItemViewModel(
                        MessageItemModel(
                            message,
                            downloaderApi,
                            sdkEventDistributor
                        )
                    )
                }
                _messages.value = messageViewModels
            } catch (e: Exception) {
                _messages.value = emptyList()
            } finally {
                _isRefreshing.value = false
            }
        }
    }

}