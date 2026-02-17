package com.sap.ec.mobileengage.embeddedmessaging.ui.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.paging.compose.collectAsLazyPagingItems
import com.sap.ec.di.SdkKoinIsolationContext.koin
import com.sap.ec.mobileengage.embeddedmessaging.ui.theme.EmbeddedMessagingStyleSheet
import com.sap.ec.mobileengage.embeddedmessaging.ui.theme.EmbeddedMessagingTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.dom.Div

@Composable
fun CompactListView(
    customMessageItemElementName: String? = null,
    navigateToDetailedView: () -> Unit = {}
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
                    customMessageItemElementName,
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