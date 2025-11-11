package com.emarsys.mobileengage.embeddedmessaging.ui.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.emarsys.core.util.DownloaderApi
import com.emarsys.di.SdkKoinIsolationContext.koin
import com.emarsys.mobileengage.embeddedmessaging.ui.item.MessageItemView
import com.emarsys.mobileengage.embeddedmessaging.ui.item.MessageItemViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ListPageView(
    viewModel: ListPageViewModelApi
) {
    Column {
        MessageList(viewModel)
    }
}

@Composable
fun MessageList(viewModel: ListPageViewModelApi) {
    var messages by remember { mutableStateOf<List<MessageItemViewModel>>(emptyList()) }

    LaunchedEffect(Unit) {
        viewModel.refreshMessages()

        viewModel.messages.collect { newMessages ->
            messages = newMessages
        }
    }

    LazyColumn {
        items(items = messages, key = { it.id }) { messageViewModel ->
            MessageItemView(messageViewModel)
        }
    }
}
