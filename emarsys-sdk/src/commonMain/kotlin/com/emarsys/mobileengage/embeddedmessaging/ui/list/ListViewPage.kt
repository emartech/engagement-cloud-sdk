package com.emarsys.mobileengage.embeddedmessaging.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emarsys.di.SdkKoinIsolationContext.koin
import com.emarsys.mobileengage.embeddedmessaging.ui.item.MessageItemView
import com.emarsys.mobileengage.embeddedmessaging.ui.item.MessageItemViewModel

@Composable
fun ListPageView(
    viewModel: ListPageViewModelApi = koin.get()
) {
    Column {
        MessageList(viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageList(viewModel: ListPageViewModelApi) {
    var messages by remember { mutableStateOf<List<MessageItemViewModel>>(emptyList()) }
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val filterUnreadOnly by viewModel.filterUnreadOnly.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshMessages()

        viewModel.messages.collect { newMessages ->
            messages = newMessages
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refreshMessages() },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            FilterRow(
                filterUnreadOnly = filterUnreadOnly,
                onFilterChange = { viewModel.setFilterUnreadOnly(it) }
            )
            
            if (messages.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(items = messages, key = { it.id }) { messageViewModel ->
                        MessageItemView(messageViewModel)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterRow(
    filterUnreadOnly: Boolean,
    onFilterChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = !filterUnreadOnly,
            onClick = { onFilterChange(false) },
            label = { Text("All") }
        )
        FilterChip(
            selected = filterUnreadOnly,
            onClick = { onFilterChange(true) },
            label = { Text("Unread") }
        )
    }
}

@Composable
fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("No messages")
            Text("You have no messages in the selected view.")
        }
    }
}
