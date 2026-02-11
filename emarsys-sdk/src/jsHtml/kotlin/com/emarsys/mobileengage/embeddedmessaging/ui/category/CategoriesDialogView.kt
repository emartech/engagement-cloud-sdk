package com.emarsys.mobileengage.embeddedmessaging.ui.category

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.emarsys.mobileengage.embeddedmessaging.ui.theme.EmbeddedMessagingStyleSheet
import com.emarsys.mobileengage.embeddedmessaging.ui.translation.LocalStringResources
import com.emarsys.networking.clients.embedded.messaging.model.MessageCategory
import org.jetbrains.compose.web.ExperimentalComposeWebSvgApi
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Hr
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.svg.Path
import org.jetbrains.compose.web.svg.Svg

private const val CLOSE_ICON_PATH =
    "M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"
private const val CHECK_ICON_PATH =
    "M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"

@Composable
fun CategoriesDialogView(
    categories: List<MessageCategory>,
    selectedCategories: Set<Int>,
    onApplyClicked: (Set<Int>) -> Unit,
    onDismiss: () -> Unit
) {
    val selectedCategories = remember { mutableStateOf(selectedCategories) }

    Div({
        classes(EmbeddedMessagingStyleSheet.dialogOverlay)
        onClick { onDismiss() }
    }) {
        Div({
            classes(EmbeddedMessagingStyleSheet.dialogCard)
            onClick { it.stopPropagation() }
        }) {
            Div({
                classes(EmbeddedMessagingStyleSheet.dialogContent)
            }) {
                DialogHeader(onDismiss)

                CategoryFilterChipsList(categories, selectedCategories)

                Hr({
                    classes(EmbeddedMessagingStyleSheet.dialogDivider)
                })

                DialogActionButtons(selectedCategories, onApplyClicked = onApplyClicked)
            }
        }
    }
}

@Composable
private fun DialogHeader(onDismiss: () -> Unit) {
    Div({
        classes(EmbeddedMessagingStyleSheet.dialogHeaderContainer)
    }) {
        Div({
            classes(EmbeddedMessagingStyleSheet.dialogHeaderRow)
        }) {
            Span({
                classes(EmbeddedMessagingStyleSheet.dialogTitle)
            }) {
                Text(LocalStringResources.current.categoriesFilterDialogTitle)
            }

            Button({
                onClick { onDismiss() }
                classes(EmbeddedMessagingStyleSheet.dialogCloseButton)
            }) {
                SvgIcon(path = CLOSE_ICON_PATH)
            }
        }

        Span({
            classes(EmbeddedMessagingStyleSheet.dialogSubtitle)
        }) {
            Text(LocalStringResources.current.categoriesFilterDialogSubtitle)
        }
    }
}

@Composable
private fun CategoryFilterChipsList(
    categories: List<MessageCategory>,
    selectedCategories: MutableState<Set<Int>>
) {
    Div({
        classes(EmbeddedMessagingStyleSheet.categoryChipsContainer)
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
                classes(
                    EmbeddedMessagingStyleSheet.categoryChip,
                    if (isSelected) EmbeddedMessagingStyleSheet.categoryChipSelected
                    else EmbeddedMessagingStyleSheet.categoryChipUnselected
                )
            }) {
                if (isSelected) {
                    SvgIcon(path = CHECK_ICON_PATH)
                }
                Text(value)
            }
        }
    }
}

@Composable
private fun DialogActionButtons(
    selectedCategories: MutableState<Set<Int>>,
    onApplyClicked: (Set<Int>) -> Unit
) {
    Div({
        classes(EmbeddedMessagingStyleSheet.dialogActionsContainer)
    }) {
        Button({
            onClick {
                selectedCategories.value = emptySet()
            }
            classes(
                EmbeddedMessagingStyleSheet.dialogButton,
                EmbeddedMessagingStyleSheet.categoriesDialogResetButton
            )
        }) {
            Text(LocalStringResources.current.categoriesFilterDialogResetButtonLabel)
        }

        Div({
            classes(EmbeddedMessagingStyleSheet.spacer)
        })

        Button({
            onClick {
                onApplyClicked(selectedCategories.value)
            }
            classes(
                EmbeddedMessagingStyleSheet.dialogButton,
                EmbeddedMessagingStyleSheet.categoriesDialogApplyButton
            )
        }) {
            Text(LocalStringResources.current.categoriesFilterDialogApplyButtonLabel)
        }
    }
}

@OptIn(ExperimentalComposeWebSvgApi::class)
@Composable
internal fun SvgIcon(path: String, className: String? = null) {
    Svg(viewBox = "0 0 24 24", attrs = {
        classes(EmbeddedMessagingStyleSheet.defaultSvgIconSize)
        className?.let { classes(it) }
        attr("fill", "currentColor")
    }) {
        Path(d = path)
    }
}
