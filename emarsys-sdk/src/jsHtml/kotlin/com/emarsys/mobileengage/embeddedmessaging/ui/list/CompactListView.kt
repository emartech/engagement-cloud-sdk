package com.emarsys.mobileengage.embeddedmessaging.ui.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.paging.compose.collectAsLazyPagingItems
import com.emarsys.di.SdkKoinIsolationContext.koin
import com.emarsys.mobileengage.embeddedmessaging.ui.theme.EmbeddedMessagingStyleSheet
import com.emarsys.mobileengage.embeddedmessaging.ui.theme.EmbeddedMessagingTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.dom.Div

@Composable
fun CompactListView(
    navigateToDetailedView: () -> Unit
) {
    val viewModel: ListPageViewModelApi = koin.get()
    val scope = rememberCoroutineScope()

    EmbeddedMessagingTheme {
        Div({ classes(EmbeddedMessagingStyleSheet.compactListView) }) {
            Div({
                classes(EmbeddedMessagingStyleSheet.messageListContainer)
            }) {
                ListView(
                    lazyPagingMessageItems = viewModel.messagePagingDataFlowFiltered.collectAsLazyPagingItems(),
                    viewModel,
                    onItemClick = {
                        scope.launch {
                            viewModel.selectMessage(it) {
                                navigateToDetailedView()
                            }
                        }
                    },
                    withDeleteIcon = false
                )
            }
        }
    }
}