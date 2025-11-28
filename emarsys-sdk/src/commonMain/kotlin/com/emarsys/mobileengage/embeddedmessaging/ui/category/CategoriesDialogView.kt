package com.emarsys.mobileengage.embeddedmessaging.ui.category

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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.DEFAULT_PADDING
import com.emarsys.mobileengage.embeddedmessaging.ui.theme.EmbeddedMessagingTheme
import com.emarsys.networking.clients.embedded.messaging.model.MessageCategory


@Composable
fun CategoriesDialogView(
    categories: List<MessageCategory>,
    selectedCategories: Set<Int>,
    onApplyClicked: (Set<Int>) -> Unit,
    onDismiss: () -> Unit
) {
    val selectedCategories = rememberSaveable { mutableStateOf(selectedCategories) }

    EmbeddedMessagingTheme {
        Dialog(
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            ),
            onDismissRequest = { onDismiss() }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier.padding(top = DEFAULT_PADDING),
                    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
                ) {
                    DialogHeader(onDismiss)

                    CategoryFilterChipsList(categories, selectedCategories)

                    HorizontalDivider(
                        modifier = Modifier.padding(
                            start = DEFAULT_PADDING,
                            end = DEFAULT_PADDING
                        )
                    )

                    DialogActionButtons(selectedCategories, onApplyClicked = onApplyClicked)
                }
            }
        }
    }
}

@Composable
private fun DialogHeader(onDismiss: () -> Unit) {
    EmbeddedMessagingTheme {
        Row(
            modifier = Modifier.padding(start = DEFAULT_PADDING),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Categories", style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = { onDismiss() }) {
                Icon(Icons.Outlined.Close, contentDescription = "Close categories dialog")
            }
        }

        Text(
            "Select Category Filters",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(start = DEFAULT_PADDING)
        )
    }
}

@Composable
private fun CategoryFilterChipsList(
    categories: List<MessageCategory>,
    selectedCategories: MutableState<Set<Int>>
) {
    EmbeddedMessagingTheme {
        FlowRow(
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            categories.forEach { (id, value) ->
                FilterChip(
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
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (selectedCategories.value.contains(id)) {
                                Icon(
                                    Icons.Outlined.Check,
                                    contentDescription = "Category $value selected"
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
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
                onClick = {
                    selectedCategories.value = emptySet()
                },
            ) {
                Text("Reset")
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                onClick = {
                    onApplyClicked(selectedCategories.value)
                },
            ) {
                Text("Apply")
            }
        }
    }
}
