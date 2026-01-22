package com.emarsys.mobileengage.embeddedmessaging.ui.list

import androidx.paging.cachedIn
import androidx.paging.map
import com.emarsys.core.providers.InstantProvider
import com.emarsys.mobileengage.embeddedmessaging.EmbeddedMessagingContextApi
import com.emarsys.mobileengage.embeddedmessaging.ui.item.MessageItemViewModelApi
import com.emarsys.networking.clients.embedded.messaging.model.MessageCategory
import com.emarsys.watchdog.connection.ConnectionWatchDog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
internal class ListPageViewModel(
    private val embeddedMessagingContext: EmbeddedMessagingContextApi,
    private val timestampProvider: InstantProvider,
    coroutineScope: CoroutineScope,
    private val pagerFactory: PagerFactoryApi,
    connectionWatchDog: ConnectionWatchDog,
    private val locallyDeletedMessageIds: MutableStateFlow<Set<String>>,
    private val locallyReadMessageIds: MutableStateFlow<Set<String>>
) : ListPageViewModelApi {
    private val _categories = MutableStateFlow<List<MessageCategory>>(emptyList())
    override val categories: StateFlow<List<MessageCategory>> = _categories.asStateFlow()

    private val _filterUnreadOnly = MutableStateFlow(false)
    override val filterUnreadOnly: StateFlow<Boolean> = _filterUnreadOnly.asStateFlow()
    private val _selectedCategoryIds = MutableStateFlow<Set<Int>>(emptySet())
    override val selectedCategoryIds: StateFlow<Set<Int>> = _selectedCategoryIds.asStateFlow()
    override val hasConnection: StateFlow<Boolean> = connectionWatchDog.isOnline
    override val hasFiltersApplied: StateFlow<Boolean> =
        combine(_filterUnreadOnly, _selectedCategoryIds) { unreadOnly, categoryIds ->
            unreadOnly || categoryIds.isNotEmpty()
        }.stateIn(coroutineScope, SharingStarted.Eagerly, false)

    private val _selectedMessage: MutableStateFlow<MessageItemViewModelApi?> = MutableStateFlow(null)
    override val selectedMessage: StateFlow<MessageItemViewModelApi?> = _selectedMessage.asStateFlow()

    private val _showCategorySelector = MutableStateFlow(false)
    override val showCategorySelector: StateFlow<Boolean> = _showCategorySelector.asStateFlow()

    private var lastRefreshTimestamp: Instant? = null

    private val triggerFromJS = MutableStateFlow(false)

    private val _shouldHideFilterRowForDetailedView = MutableStateFlow(false)

    override val shouldHideFilterRowForDetailedView: StateFlow<Boolean> = _shouldHideFilterRowForDetailedView.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _messagePagingDataFlowFromApi =
        combine(
            _filterUnreadOnly,
            _selectedCategoryIds,
            triggerFromJS
        ) { filterUnreadOnly, selectedCategoryIds, _ ->
            Pair(filterUnreadOnly, selectedCategoryIds)
        }
            .flatMapLatest { pair ->
                val (filterUnreadOnly, selectedCategoryIds) = pair
                lastRefreshTimestamp = null
                pagerFactory.create(
                    filterUnreadOnly = filterUnreadOnly,
                    selectedCategoryIds = selectedCategoryIds.toList(),
                    categories = _categories
                )
            }
            .map { pagingData ->
                pagingData.map {
                    if ((!it.isExcludedLocally && filterUnreadOnly.value && locallyReadMessageIds.value.contains(
                            it.id
                        )) || it.isDeleted
                    ) {
                        it.copyAsExcludedLocally()
                    } else it
                }
            }.cachedIn(coroutineScope)

    override val messagePagingDataFlowFiltered =
        combine(
            _messagePagingDataFlowFromApi,
            locallyDeletedMessageIds,
        ) { pagingData, deletedIds ->
            pagingData.map {
                if (!it.isExcludedLocally && deletedIds.contains(it.id)) {
                    it.copyAsExcludedLocally()
                } else it
            }
        }

    override val triggerRefreshFromJs = { triggerFromJS.value = !triggerFromJS.value }

    override fun setFilterUnreadOnly(unreadOnly: Boolean) {
        _filterUnreadOnly.value = unreadOnly
    }

    override fun setSelectedCategoryIds(categoryIds: Set<Int>) {
        _selectedCategoryIds.value = categoryIds
    }

    override fun hideFilterRowForDetailedView(shouldHideFilterRow: Boolean) {
        _shouldHideFilterRowForDetailedView.value = shouldHideFilterRow
    }

    override suspend fun selectMessage(
        messageViewModel: MessageItemViewModelApi,
        onNavigate: suspend () -> Unit
    ) {
        _selectedMessage.value = messageViewModel

        if(!locallyReadMessageIds.value.contains(messageViewModel.id) && messageViewModel.isUnread) {
            messageViewModel.tagMessageRead()
                .onSuccess {
                    val newReadIdSet = mutableSetOf(messageViewModel.id)
                    newReadIdSet.addAll(locallyReadMessageIds.value)
                    locallyReadMessageIds.value = newReadIdSet
                }
        }

        if (messageViewModel.shouldNavigate()) {
            onNavigate.invoke()
        }
        messageViewModel.handleDefaultAction()
    }

    override suspend fun deleteMessage(
        messageViewModel: MessageItemViewModelApi
    ): Result<Unit> {
        return messageViewModel.deleteMessage()
            .onSuccess {
                val newDeletedIdSet = mutableSetOf(messageViewModel.id)
                newDeletedIdSet.addAll(locallyDeletedMessageIds.value)
                locallyDeletedMessageIds.value = newDeletedIdSet
                if (_selectedMessage.value?.id == messageViewModel.id) {
                    clearMessageSelection()
                }
            }
    }

    override fun clearMessageSelection() {
        _selectedMessage.value = null
    }

    override fun openCategorySelector() {
        _showCategorySelector.value = true
    }

    override fun closeCategorySelector() {
        _showCategorySelector.value = false
    }

    override fun applyCategorySelection(categoryIds: Set<Int>) {
        setSelectedCategoryIds(categoryIds)
        closeCategorySelector()
    }

    override fun refreshMessagesWithThrottling(shouldCallRefresh: () -> Unit) {
        if (!isTooFrequentFetch()) {
            shouldCallRefresh.invoke()
            lastRefreshTimestamp = timestampProvider.provide()
        }
    }

    private fun isTooFrequentFetch(): Boolean {
        return lastRefreshTimestamp?.let {
            (timestampProvider.provide() - lastRefreshTimestamp!!).inWholeSeconds < embeddedMessagingContext.embeddedMessagingFrequencyCapSeconds
        } ?: false
    }
}