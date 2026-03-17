package com.sap.ec.mobileengage.embeddedmessaging.ui.list

import androidx.paging.cachedIn
import androidx.paging.map
import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.providers.InstantProvider
import com.sap.ec.core.providers.inputmode.InputModeProviderApi
import com.sap.ec.core.providers.platform.PlatformCategoryProviderApi
import com.sap.ec.event.SdkEvent
import com.sap.ec.mobileengage.embeddedmessaging.EmbeddedMessagingContextApi
import com.sap.ec.mobileengage.embeddedmessaging.ui.item.MessageItemViewModelApi
import com.sap.ec.networking.clients.embedded.messaging.model.Category
import com.sap.ec.watchdog.connection.ConnectionWatchDog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
internal class ListPageViewModel(
    private val embeddedMessagingContext: EmbeddedMessagingContextApi,
    private val timestampProvider: InstantProvider,
    private val coroutineScope: CoroutineScope,
    private val pagerFactory: PagerFactoryApi,
    connectionWatchDog: ConnectionWatchDog,
    private val locallyDeletedMessageIds: MutableStateFlow<Set<String>>,
    private val locallyOpenedMessageIds: MutableStateFlow<Set<String>>,
    platformCategoryProvider: PlatformCategoryProviderApi,
    inputModeProvider: InputModeProviderApi,
    sdkEventDistributor: SdkEventDistributorApi,
    private val _categories: MutableStateFlow<List<Category>>
) : ListPageViewModelApi {
    override val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _filterUnopenedOnly = MutableStateFlow(false)
    override val filterUnopenedOnly: StateFlow<Boolean> = _filterUnopenedOnly.asStateFlow()
    private val _selectedCategoryIds = MutableStateFlow<Set<String>>(emptySet())
    override val selectedCategoryIds: StateFlow<Set<String>> = _selectedCategoryIds.asStateFlow()
    override val hasConnection: StateFlow<Boolean> = connectionWatchDog.isOnline
    override val hasFiltersApplied: StateFlow<Boolean> =
        combine(_filterUnopenedOnly, _selectedCategoryIds) { unreadOnly, categoryIds ->
            unreadOnly || categoryIds.isNotEmpty()
        }.stateIn(coroutineScope, SharingStarted.Eagerly, false)

    private val _selectedMessage: MutableStateFlow<MessageItemViewModelApi?> =
        MutableStateFlow(null)
    override val selectedMessage: StateFlow<MessageItemViewModelApi?> =
        _selectedMessage.asStateFlow()

    private var selectionJob: Job? = null

    private val _showCategorySelector = MutableStateFlow(false)
    override val showCategorySelector: StateFlow<Boolean> = _showCategorySelector.asStateFlow()

    private var lastRefreshTimestamp: Instant? = null

    private val refreshTrigger = MutableStateFlow(false)

    override val platformCategory: String = platformCategoryProvider.provide()

    override val hasTouchInput: Boolean = inputModeProvider.hasTouchSupport()

    init {
        coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
            sdkEventDistributor.sdkEventFlow
                .filter { it is SdkEvent.Internal.EmbeddedMessaging.TriggerRefresh }
                .collect {
                    _categories.value = emptyList()
                    refreshTrigger.value = !refreshTrigger.value
                }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _messagePagingDataFlowFromApi =
        combine(
            _filterUnopenedOnly,
            _selectedCategoryIds,
            refreshTrigger
        ) { filterUnopenedOnly, selectedCategoryIds, _ ->
            Pair(filterUnopenedOnly, selectedCategoryIds)
        }
            .flatMapLatest { pair ->
                val (filterUnopenedOnly, selectedCategoryIds) = pair
                pagerFactory.create(
                    filterUnopenedOnly = filterUnopenedOnly,
                    selectedCategoryIds = selectedCategoryIds.toList(),
                    categories = _categories
                )
            }
            .map { pagingData ->
                pagingData.map {
                    if ((!it.isExcludedLocally && filterUnopenedOnly.value && locallyOpenedMessageIds.value.contains(
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
            locallyOpenedMessageIds
        ) { pagingData, deletedIds, openedIds ->
            pagingData.map { messageItemViewModel ->
                val shouldBeExcludedLocally =
                    !messageItemViewModel.isExcludedLocally && deletedIds.contains(
                        messageItemViewModel.id
                    )
                val isOpenedLocally = openedIds.contains(messageItemViewModel.id)
                messageItemViewModel.let {
                    if (shouldBeExcludedLocally) {
                        it.copyAsExcludedLocally()
                    } else it
                }.let {
                    if (isOpenedLocally) {
                        it.copyAsOpenedLocally()
                    } else it
                }
            }
        }

    override val triggerRefreshFromJs = { refreshTrigger.value = !refreshTrigger.value }

    override fun setFilterUnopenedOnly(unopenedOnly: Boolean) {
        _filterUnopenedOnly.value = unopenedOnly
    }

    override fun setSelectedCategoryIds(categoryIds: Set<String>) {
        val existingCategoryIds = categoryIds.filter { id ->
            _categories.value.any { category ->
                category.id == id
            }
        }.toSet()

        _selectedCategoryIds.value = existingCategoryIds

        val selectedMessageIncludedInFilteredCategories =
            (selectedMessage.value?.categories?.any {
                _selectedCategoryIds.value.contains(it.id)
            }) ?: false

        if (!selectedMessageIncludedInFilteredCategories) {
            clearMessageSelection()
        }
    }

    override fun selectMessage(
        messageViewModel: MessageItemViewModelApi,
        onNavigate: suspend () -> Unit
    ) {
        selectionJob?.cancel()
        _selectedMessage.value = messageViewModel
        selectionJob = coroutineScope.launch {
            if (!locallyOpenedMessageIds.value.contains(messageViewModel.id) && messageViewModel.isNotOpened) {
                messageViewModel.tagMessageOpened()
                    .onSuccess {
                        if (_selectedMessage.value == messageViewModel) {
                            val newOpenedIdSet = mutableSetOf(messageViewModel.id)
                            newOpenedIdSet.addAll(locallyOpenedMessageIds.value)
                            locallyOpenedMessageIds.value = newOpenedIdSet
                        }
                    }
            }

            if (_selectedMessage.value != messageViewModel) return@launch

            if (messageViewModel.hasRichContent()) {
                onNavigate.invoke()
            } else {
                // Fire-and-forget: read tagging failures are non-critical and do not affect UX
                messageViewModel.tagMessageRead().onFailure { }
                messageViewModel.handleDefaultAction()
            }
        }
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

    override suspend fun tagMessageRead(messageViewModel: MessageItemViewModelApi): Result<Unit> {
        return if (!messageViewModel.isRead) {
            messageViewModel.tagMessageRead()
        } else {
            Result.success(Unit)
        }
    }

    override fun clearMessageSelection() {
        selectionJob?.cancel()
        selectionJob = null
        _selectedMessage.value = null
    }

    override fun openCategorySelector() {
        _showCategorySelector.value = true
    }

    override fun closeCategorySelector() {
        _showCategorySelector.value = false
    }

    override fun applyCategorySelection(categoryIds: Set<String>) {
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