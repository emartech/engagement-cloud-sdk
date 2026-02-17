package com.sap.ec.mobileengage.embeddedmessaging.ui.list

import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.sap.ec.mobileengage.embeddedmessaging.ui.item.MessageItemViewModelApi

internal fun LazyPagingItems<MessageItemViewModelApi>.isIdleButEmpty(): Boolean {
    val itemCountWithoutLocallyExcluded =
        this.itemSnapshotList.count { it?.isExcludedLocally == false }
    return itemCountWithoutLocallyExcluded == 0 && this.loadState.isIdle && !this.loadState.hasError
}

internal fun LazyPagingItems<MessageItemViewModelApi>.isInitiallyLoading(): Boolean =
    this.loadState.source.refresh is LoadState.Loading

internal fun LazyPagingItems<MessageItemViewModelApi>.hasRefreshError(): Boolean =
    this.loadState.source.refresh is LoadState.Error

internal fun LazyPagingItems<MessageItemViewModelApi>.isLoadingMore(): Boolean =
    this.loadState.source.append == LoadState.Loading