package com.emarsys.mobileengage.embeddedmessaging.ui.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.emarsys.di.SdkKoinIsolationContext.koin
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.DEFAULT_PADDING
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.FLOATING_ACTION_BUTTON_SIZE
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.ZERO_SPACING
import com.emarsys.mobileengage.embeddedmessaging.ui.category.CategoriesDialogView
import com.emarsys.mobileengage.embeddedmessaging.ui.category.CategorySelectorButton
import com.emarsys.mobileengage.embeddedmessaging.ui.item.MessageItemView
import com.emarsys.mobileengage.embeddedmessaging.ui.theme.EmbeddedMessagingTheme
import com.emarsys.mobileengage.embeddedmessaging.ui.translation.LocalStringResources
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.border
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.cursor
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flex
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.margin
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.marginRight
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.textAlign
import org.jetbrains.compose.web.css.vh
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Hr
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun ListPageView(
    viewModel: ListPageViewModelApi = koin.get()
) {
    EmbeddedMessagingTheme {
        Div({
            style {
                backgroundColor(Color("var(--color-surface)"))
                height(100.vh)
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Column)
            }
        }) {
            MessageList(viewModel)
        }
    }
}

@Composable
fun MessageList(viewModel: ListPageViewModelApi) {
    val messages by viewModel.messages.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val filterUnreadOnly by viewModel.filterUnreadOnly.collectAsState()
    val selectedCategoryIds by viewModel.selectedCategoryIds.collectAsState()
    var showCategorySelector by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refreshMessages()
    }

    EmbeddedMessagingTheme {
        FilterRow(
            selectedCategoryIds = selectedCategoryIds,
            filterUnreadOnly = filterUnreadOnly,
            onFilterChange = { viewModel.setFilterUnreadOnly(it) },
            onCategorySelectorClicked = {
                showCategorySelector = true
            }
        )

        Hr({
            style {
                border(0.px)
                height(1.px)
                backgroundColor(Color("var(--color-outline)"))
                margin(0.px)
            }
        })

        if (showCategorySelector) {
            CategoriesDialogView(
                categories = viewModel.categories.collectAsState().value,
                selectedCategories = viewModel.selectedCategoryIds.collectAsState().value,
                onApplyClicked = {
                    viewModel.setSelectedCategoryIds(it)
                    viewModel.refreshMessages()
                    showCategorySelector = false
                },
                onDismiss = {
                    showCategorySelector = false
                }
            )
        }

        // Container with refresh button and scrollable list
        Div({
            style {
                flex(1)
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Column)
                property("overflow", "hidden")
            }
        }) {
            // Refresh button (simpler alternative to pull-to-refresh)
            if (isRefreshing) {
                Div({
                    style {
                        padding(8.px)
                        textAlign("center")
                        property("color", "var(--color-on-surface)")
                        fontSize(14.px)
                    }
                }) {
                    Text("Refreshing...")
                }
            } else {
                Button({
                    onClick { viewModel.refreshMessages() }
                    style {
                        width(100.percent)
                        padding(8.px)
                        border(0.px)
                        backgroundColor(Color("var(--color-surface-variant)"))
                        color(Color("var(--color-on-surface-variant)"))
                        cursor("pointer")
                        fontSize(14.px)
                    }
                }) {
                    Text("↻ Refresh")
                }
            }

            // Scrollable message list
            if (messages.isEmpty()) {
                EmptyState()
            } else {
                Div({
                    style {
                        flex(1)
                        property("overflow-y", "auto")
                    }
                }) {
                    messages.forEach { messageViewModel ->
                        MessageItemView(messageViewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun FilterRow(
    selectedCategoryIds: Set<Int>,
    filterUnreadOnly: Boolean,
    onFilterChange: (Boolean) -> Unit,
    onCategorySelectorClicked: () -> Unit
) {
    EmbeddedMessagingTheme {
        Div({
            style {
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                padding(DEFAULT_PADDING)
                gap(ZERO_SPACING)
            }
        }) {
            // All Messages filter chip
            Button({
                onClick { onFilterChange(false) }
                style {
                    height(FLOATING_ACTION_BUTTON_SIZE)
                    padding(8.px, 12.px)
                    borderRadius(16.px)
                    cursor("pointer")
                    fontSize(14.px)
                    fontWeight(400)
                    marginRight(8.px)
                    
                    if (!filterUnreadOnly) {
                        backgroundColor(Color("var(--color-secondary-container)"))
                        color(Color("var(--color-on-secondary-container)"))
                        border(0.px)
                    } else {
                        backgroundColor(Color.transparent)
                        color(Color("var(--color-on-surface-variant)"))
                        property("border", "1px solid var(--color-outline)")
                    }
                }
            }) {
                Text(LocalStringResources.current.allMessagesFilterButtonLabel)
            }
            
            // Unread Messages filter chip
            Button({
                onClick { onFilterChange(true) }
                style {
                    height(FLOATING_ACTION_BUTTON_SIZE)
                    padding(8.px, 12.px)
                    borderRadius(16.px)
                    cursor("pointer")
                    fontSize(14.px)
                    fontWeight(400)
                    
                    if (filterUnreadOnly) {
                        backgroundColor(Color("var(--color-secondary-container)"))
                        color(Color("var(--color-on-secondary-container)"))
                        border(0.px)
                    } else {
                        backgroundColor(Color.transparent)
                        color(Color("var(--color-on-surface-variant)"))
                        property("border", "1px solid var(--color-outline)")
                    }
                }
            }) {
                Text(LocalStringResources.current.unreadMessagesFilterButtonLabel)
            }

            Div({ style { flex(1) } })

            CategorySelectorButton(
                isCategorySelectionActive = selectedCategoryIds.isNotEmpty(),
                onClick = {
                    onCategorySelectorClicked()
                },
            )
        }
    }
}

@Composable
fun EmptyState() {
    EmbeddedMessagingTheme {
        Div({
            style {
                flex(1)
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                justifyContent(JustifyContent.Center)
                property("overflow-y", "auto")
            }
        }) {
            Div({
                style {
                    display(DisplayStyle.Flex)
                    flexDirection(FlexDirection.Column)
                    alignItems(AlignItems.Center)
                    textAlign("center")
                }
            }) {
                Span({
                    style {
                        fontSize(16.px)
                        property("color", "var(--color-on-surface)")
                        display(DisplayStyle.Block)
                        marginBottom(8.px)
                    }
                }) {
                    Text(LocalStringResources.current.emptyStateTitle)
                }
                Span({
                    style {
                        fontSize(16.px)
                        property("color", "var(--color-on-surface)")
                        display(DisplayStyle.Block)
                    }
                }) {
                    Text(LocalStringResources.current.emptyStateDescription)
                }
            }
        }
    }
}
