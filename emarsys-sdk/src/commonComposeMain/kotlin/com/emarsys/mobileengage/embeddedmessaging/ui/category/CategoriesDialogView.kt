package com.emarsys.mobileengage.embeddedmessaging.ui.category

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.Dimensions.DEFAULT_PADDING
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.Dimensions.DEFAULT_SPACING
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.Dimensions.DIALOG_CONTAINER_PADDING
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.Dimensions.LARGE_PADDING
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.Dimensions.ZERO_ELEVATION
import com.emarsys.mobileengage.embeddedmessaging.ui.theme.EmbeddedMessagingTheme
import com.emarsys.mobileengage.embeddedmessaging.ui.theme.LocalDesignValues
import com.emarsys.mobileengage.embeddedmessaging.ui.translation.LocalStringResources
import com.emarsys.networking.clients.embedded.messaging.model.MessageCategory


@Composable
fun CategoriesDialogView(
    categories: List<MessageCategory>,
    selectedCategoriesOnDialogOpen: Set<Int>,
    onApplyClicked: (Set<Int>) -> Unit,
    onDismiss: () -> Unit
) {
    val selectedCategories = rememberSaveable { mutableStateOf(selectedCategoriesOnDialogOpen) }

    EmbeddedMessagingTheme {
        Dialog(
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            ),
            onDismissRequest = { onDismiss() }
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier.padding(DIALOG_CONTAINER_PADDING),
                    verticalArrangement = Arrangement.spacedBy(
                        DEFAULT_SPACING,
                        Alignment.CenterVertically
                    )
                ) {
                    DialogHeader(onDismiss)

                    CategoryFilterChipsList(categories, selectedCategories)

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(
                            start = DEFAULT_PADDING,
                            end = DEFAULT_PADDING
                        )
                    )

                    DialogActionButtons(
                        selectedCategories,
                        onApplyClicked = onApplyClicked
                    )
                }
            }
        }
    }
}

@Composable
private fun DialogHeader(onDismiss: () -> Unit) {
    EmbeddedMessagingTheme {
        Column(modifier = Modifier.padding(start = DEFAULT_PADDING)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = LocalStringResources.current.categoriesFilterDialogTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.W400
                )

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = { onDismiss() }) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = LocalStringResources.current.closeIconButtonAltText
                    )
                }
            }

            Text(
                modifier = Modifier.padding(top = LARGE_PADDING),
                text = LocalStringResources.current.categoriesFilterDialogSubtitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.W500
            )
        }
    }
}

@Composable
private fun CategoryFilterChipsList(
    categories: List<MessageCategory>,
    selectedCategories: MutableState<Set<Int>>
) {
    EmbeddedMessagingTheme {
        FlowRow(
            verticalArrangement = Arrangement.spacedBy(DEFAULT_SPACING, Alignment.CenterVertically),
            horizontalArrangement = Arrangement.spacedBy(DEFAULT_SPACING),
            modifier = Modifier
                .fillMaxWidth()
                .padding(DEFAULT_PADDING)
        ) {
            categories.forEach { (id, value) ->
                FilterChip(
                    border = filterChipBorderSettings(selectedCategories, id),
                    colors = filterChipColors(),
                    elevation = FilterChipDefaults.filterChipElevation(
                        hoveredElevation = 0.dp,
                    ),
                    selected = selectedCategories.value.contains(id),
                    onClick = {
                        selectedCategories.value =
                            if (selectedCategories.value.contains(id)) {
                                selectedCategories.value - id
                            } else {
                                selectedCategories.value + id
                            }
                    },
                    label = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(DEFAULT_SPACING),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (selectedCategories.value.contains(id)) {
                                Icon(
                                    Icons.Outlined.Check,
                                    contentDescription = "$value ${LocalStringResources.current.selectedCategoryFilterChipIconAltText}"
                                )
                            }
                            Text(value)
                        }
                    }
                )
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
        Row(
            modifier = Modifier.padding(DEFAULT_PADDING)
        ) {
            Button(
                elevation = ButtonDefaults.buttonElevation(ZERO_ELEVATION),
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                onClick = {
                    selectedCategories.value = emptySet()
                },
            ) {
                Text(LocalStringResources.current.categoriesFilterDialogResetButtonLabel)
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                elevation = ButtonDefaults.buttonElevation(LocalDesignValues.current.buttonElevation),
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                onClick = {
                    onApplyClicked(selectedCategories.value)
                },
            ) {
                Text(LocalStringResources.current.categoriesFilterDialogApplyButtonLabel)
            }
        }
    }
}

@Composable
private fun filterChipColors(): SelectableChipColors = SelectableChipColors(
    containerColor = Color.Unspecified,
    labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
    leadingIconColor = Color.Unspecified,
    trailingIconColor = Color.Unspecified,
    disabledLabelColor = Color.Unspecified,
    disabledContainerColor = Color.Unspecified,
    disabledLeadingIconColor = Color.Unspecified,
    disabledTrailingIconColor = Color.Unspecified,
    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
    disabledSelectedContainerColor = Color.Unspecified,
    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
    selectedLeadingIconColor = Color.Unspecified,
    selectedTrailingIconColor = Color.Unspecified
)

@Composable
private fun filterChipBorderSettings(
    selectedCategories: MutableState<Set<Int>>,
    id: Int
): BorderStroke = FilterChipDefaults.filterChipBorder(
    enabled = true,
    borderColor = MaterialTheme.colorScheme.outline,
    selectedBorderColor = Color.Transparent,
    selectedBorderWidth = 0.dp,
    borderWidth = 1.dp,
    selected = selectedCategories.value.contains(id)
)
