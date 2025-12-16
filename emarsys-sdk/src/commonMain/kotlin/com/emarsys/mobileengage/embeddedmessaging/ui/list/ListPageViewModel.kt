package com.emarsys.mobileengage.embeddedmessaging.ui.list

import androidx.paging.cachedIn
import com.emarsys.core.providers.InstantProvider
import com.emarsys.mobileengage.embeddedmessaging.EmbeddedMessagingContextApi
import com.emarsys.networking.clients.embedded.messaging.model.MessageCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
internal class ListPageViewModel(
    private val embeddedMessagingContext: EmbeddedMessagingContextApi,
    private val timestampProvider: InstantProvider,
    private val coroutineScope: CoroutineScope,
    private val pagerFactory: PagerFactoryApi,
) : ListPageViewModelApi {
    private val _categories = MutableStateFlow<List<MessageCategory>>(emptyList())
    override val categories: StateFlow<List<MessageCategory>> = _categories.asStateFlow()

    private val _filterUnreadOnly = MutableStateFlow(false)
    override val filterUnreadOnly: StateFlow<Boolean> = _filterUnreadOnly.asStateFlow()
    private val _selectedCategoryIds = MutableStateFlow<Set<Int>>(emptySet())
    override val selectedCategoryIds: StateFlow<Set<Int>> = _selectedCategoryIds.asStateFlow()

    private var lastRefreshTimestamp: Instant? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    override val messagePagingDataFlow =
        combine(_filterUnreadOnly, _selectedCategoryIds) { filterUnreadOnly, selectedCategoryIds ->
            filterUnreadOnly to selectedCategoryIds
        }.flatMapLatest { (filterUnreadOnly, selectedCategoryIds) ->
            lastRefreshTimestamp = null
            pagerFactory.create(
                filterUnreadOnly = filterUnreadOnly,
                selectedCategoryIds = selectedCategoryIds.toList(),
                categories = _categories
            )
        }.cachedIn(coroutineScope) // TODO: add lifecycle aware scope

    override fun setFilterUnreadOnly(unreadOnly: Boolean) {
        _filterUnreadOnly.value = unreadOnly
    }

    override fun setSelectedCategoryIds(categoryIds: Set<Int>) {
        _selectedCategoryIds.value = categoryIds
    }

    override fun refreshMessages(canCallRefresh: () -> Unit) {
        if (!isTooFrequentFetch()) {
            canCallRefresh.invoke()
            lastRefreshTimestamp = timestampProvider.provide()
        }
    }

    private fun isTooFrequentFetch(): Boolean {
        return lastRefreshTimestamp?.let {
            (timestampProvider.provide() - lastRefreshTimestamp!!).inWholeSeconds < embeddedMessagingContext.embeddedMessagingFrequencyCapSeconds
        } ?: false
    }
}