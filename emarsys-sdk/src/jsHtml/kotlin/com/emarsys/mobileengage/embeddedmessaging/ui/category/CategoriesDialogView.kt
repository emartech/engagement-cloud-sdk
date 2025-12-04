package com.emarsys.mobileengage.embeddedmessaging.ui.category

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.DEFAULT_PADDING
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.DEFAULT_SPACING
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.DIALOG_CONTAINER_PADDING
import com.emarsys.mobileengage.embeddedmessaging.ui.theme.EmbeddedMessagingTheme
import com.emarsys.mobileengage.embeddedmessaging.ui.translation.LocalStringResources
import com.emarsys.networking.clients.embedded.messaging.model.MessageCategory
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.border
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.bottom
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
import org.jetbrains.compose.web.css.left
import org.jetbrains.compose.web.css.margin
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.maxWidth
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.position
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.right
import org.jetbrains.compose.web.css.top
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Hr
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun CategoriesDialogView(
    categories: List<MessageCategory>,
    selectedCategories: Set<Int>,
    onApplyClicked: (Set<Int>) -> Unit,
    onDismiss: () -> Unit
) {
    val selectedCategories = remember { mutableStateOf(selectedCategories) }

    EmbeddedMessagingTheme {
        // Modal overlay (backdrop)
        Div({
            style {
                position(Position.Fixed)
                top(0.px)
                left(0.px)
                right(0.px)
                bottom(0.px)
                backgroundColor(Color("rgba(0, 0, 0, 0.5)"))
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                justifyContent(JustifyContent.Center)
                property("z-index", "1000")
            }
            onClick { onDismiss() }
        }) {
            // Dialog card
            Div({
                style {
                    backgroundColor(Color("var(--color-surface)"))
                    borderRadius(8.px)
                    property("box-shadow", "0 4px 16px rgba(0,0,0,0.2)")
                    maxWidth(500.px)
                    width(90.percent)
                }
                onClick { it.stopPropagation() } // Prevent closing when clicking inside
            }) {
                Div({
                    style {
                        padding(DIALOG_CONTAINER_PADDING)
                        display(DisplayStyle.Flex)
                        flexDirection(FlexDirection.Column)
                        gap(DEFAULT_SPACING)
                    }
                }) {
                    DialogHeader(onDismiss)

                    CategoryFilterChipsList(categories, selectedCategories)

                    Hr({
                        style {
                            border(0.px)
                            height(1.px)
                            backgroundColor(Color("var(--color-outline)"))
                            margin(0.px, DEFAULT_PADDING)
                        }
                    })

                    DialogActionButtons(selectedCategories, onApplyClicked = onApplyClicked)
                }
            }
        }
    }
}

@Composable
private fun DialogHeader(onDismiss: () -> Unit) {
    EmbeddedMessagingTheme {
        Div({
            style {
                padding(0.px, 0.px, 0.px, DEFAULT_PADDING)
            }
        }) {
            Div({
                style {
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                }
            }) {
                Span({
                    style {
                        fontSize(22.px)
                        fontWeight(500)
                        property("color", "var(--color-on-surface)")
                        flex(1)
                    }
                }) {
                    Text(LocalStringResources.current.categoriesFilterDialogTitle)
                }

                Button({
                    onClick { onDismiss() }
                    style {
                        border(0.px)
                        backgroundColor(Color.transparent)
                        cursor("pointer")
                        fontSize(20.px)
                        padding(8.px)
                        property("color", "var(--color-on-surface)")
                    }
                }) {
                    Text("✕")
                }
            }

            Span({
                style {
                    fontSize(14.px)
                    property("color", "var(--color-on-surface)")
                    display(DisplayStyle.Block)
                    marginTop(4.px)
                }
            }) {
                Text(LocalStringResources.current.categoriesFilterDialogSubtitle)
            }
        }
    }
}

@Composable
private fun CategoryFilterChipsList(
    categories: List<MessageCategory>,
    selectedCategories: MutableState<Set<Int>>
) {
    EmbeddedMessagingTheme {
        Div({
            style {
                display(DisplayStyle.Flex)
                property("flex-wrap", "wrap")
                gap(DEFAULT_SPACING)
                padding(DEFAULT_PADDING)
            }
        }) {
            categories.forEach { (id, value) ->
                val isSelected = selectedCategories.value.contains(id)
                
                Button({
                    onClick {
                        selectedCategories.value =
                            if (selectedCategories.value.contains(id)) {
                                selectedCategories.value - id
                            } else {
                                selectedCategories.value + id
                            }
                    }
                    style {
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        gap(DEFAULT_SPACING)
                        padding(8.px, 12.px)
                        borderRadius(16.px)
                        cursor("pointer")
                        fontSize(14.px)
                        fontWeight(400)
                        
                        if (isSelected) {
                            backgroundColor(Color("var(--color-secondary-container)"))
                            color(Color("var(--color-on-secondary-container)"))
                            border(0.px)
                        } else {
                            backgroundColor(Color.transparent)
                            color(Color("var(--color-on-primary-container)"))
                            property("border", "1px solid var(--color-outline)")
                        }
                    }
                }) {
                    if (isSelected) {
                        Span({
                            style {
                                fontSize(16.px)
                            }
                        }) {
                            Text("✓")
                        }
                    }
                    Text(value)
                }
            }
        }
    }
}

@Composable
private fun DialogActionButtons(
    selectedCategories: MutableState<Set<Int>>,
    onApplyClicked: (Set<Int>) -> Unit
) {
    EmbeddedMessagingTheme {
        Div({
            style {
                display(DisplayStyle.Flex)
                padding(DEFAULT_PADDING)
                gap(8.px)
            }
        }) {
            Button({
                onClick {
                    selectedCategories.value = emptySet()
                }
                style {
                    padding(10.px, 16.px)
                    borderRadius(4.px)
                    border(0.px)
                    backgroundColor(Color.transparent)
                    color(Color("var(--color-primary)"))
                    cursor("pointer")
                    fontSize(14.px)
                    fontWeight(500)
                }
            }) {
                Text(LocalStringResources.current.categoriesFilterDialogResetButtonLabel)
            }

            Div({ style { flex(1) } })

            Button({
                onClick {
                    onApplyClicked(selectedCategories.value)
                }
                style {
                    padding(10.px, 16.px)
                    borderRadius(4.px)
                    border(0.px)
                    backgroundColor(Color("var(--color-primary)"))
                    color(Color("var(--color-on-primary)"))
                    cursor("pointer")
                    fontSize(14.px)
                    fontWeight(500)
                    property("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
                }
            }) {
                Text(LocalStringResources.current.categoriesFilterDialogApplyButtonLabel)
            }
        }
    }
}
