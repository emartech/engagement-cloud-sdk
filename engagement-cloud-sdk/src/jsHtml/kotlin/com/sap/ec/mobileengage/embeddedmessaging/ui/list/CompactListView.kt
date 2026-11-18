package com.sap.ec.mobileengage.embeddedmessaging.ui.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.paging.compose.collectAsLazyPagingItems
import com.sap.ec.di.SdkKoinIsolationContext.koin
import com.sap.ec.mobileengage.embeddedmessaging.ui.theme.EmbeddedMessagingStyleSheet
import com.sap.ec.mobileengage.embeddedmessaging.ui.theme.EmbeddedMessagingTheme
import com.sap.ec.mobileengage.embeddedmessaging.ui.theme.LocalDesignValues
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.maxHeight
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div

@Composable
internal fun CompactListView(
    customMessageItemElementName: String? = null,
    navigateToDetailedView: () -> Unit = {}
) {
    val viewModel: ListPageViewModelApi = koin.get()
    val scope = rememberCoroutineScope()

    EmbeddedMessagingTheme {
        val designValues = LocalDesignValues.current
        Div({
            classes(EmbeddedMessagingStyleSheet.compactListView)
            style {
                designValues.compactOverlayWidth?.let { width(it) }
                designValues.compactOverlayMaxHeight?.let { maxHeight(it) }
                borderRadius(designValues.compactOverlayCornerRadius)
            }
        }) {
            Div({
                classes(EmbeddedMessagingStyleSheet.scrollingMessageListContainer)
                style { padding(designValues.listContentPadding) }
            }) {
                ListView(
                    lazyPagingMessageItems = viewModel.messagePagingDataFlowFiltered.collectAsLazyPagingItems(),
                    listViewModel = viewModel,
                    customMessageItemElementName = customMessageItemElementName,
                    onItemClick = {
                        scope.launch {
                            viewModel.selectMessage(it) {
                                navigateToDetailedView()
                            }
                        }
                    },
                    withDeleteIcon = false,
                    paginationId = "CompactListPageView"
                )
            }
        }
    }
}