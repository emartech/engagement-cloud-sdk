package com.emarsys.mobileengage.embeddedmessaging.ui.list

import androidx.paging.cachedIn
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
    private val coroutineScope: CoroutineScope,
    private val pagerFactory: PagerFactoryApi,
    private val connectionWatchDog: ConnectionWatchDog
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

    private val _selectedMessageId = MutableStateFlow<String?>(null)
    override val selectedMessageId: StateFlow<String?> = _selectedMessageId.asStateFlow()

    private var cachedSelectedMessage: MessageItemViewModelApi? = null
    override val selectedMessage: StateFlow<MessageItemViewModelApi?> =
        _selectedMessageId.map { cachedSelectedMessage }
            .stateIn(coroutineScope, SharingStarted.Eagerly, null)

    private val _showCategorySelector = MutableStateFlow(false)
    override val showCategorySelector: StateFlow<Boolean> = _showCategorySelector.asStateFlow()

    private var lastRefreshTimestamp: Instant? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    override val messagePagingDataFlow =
        combine(_filterUnreadOnly, _selectedCategoryIds) { filterUnreadOnly, selectedCategoryIds ->
            filterUnreadOnly to selectedCategoryIds
        }
            .flatMapLatest { pair ->
                val (filterUnreadOnly, selectedCategoryIds) = pair
                lastRefreshTimestamp = null
                pagerFactory.create(
                    filterUnreadOnly = filterUnreadOnly,
                    selectedCategoryIds = selectedCategoryIds.toList(),
                    categories = _categories
                )
            }.cachedIn(coroutineScope)

    override fun setFilterUnreadOnly(unreadOnly: Boolean) {
        _filterUnreadOnly.value = unreadOnly
    }

    override fun setSelectedCategoryIds(categoryIds: Set<Int>) {
        _selectedCategoryIds.value = categoryIds
    }

    override suspend fun selectMessage(
        messageViewModel: MessageItemViewModelApi,
        onNavigate: suspend () -> Unit
    ) {
        cachedSelectedMessage = messageViewModel
        _selectedMessageId.value = messageViewModel.id

        messageViewModel.tagMessageRead()
        messageViewModel.handleDefaultAction()

        if (messageViewModel.shouldNavigate()) {
            onNavigate.invoke()
        }
    }

    override fun clearMessageSelection() {
        _selectedMessageId.value = null
        cachedSelectedMessage = null
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

    override fun refreshMessages(shouldCallRefresh: () -> Unit) {
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