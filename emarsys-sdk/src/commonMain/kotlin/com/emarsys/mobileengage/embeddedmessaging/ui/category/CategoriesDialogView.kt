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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.emarsys.networking.clients.embedded.messaging.model.MessageCategory


@Composable
fun CategoriesDialogView(categories: List<MessageCategory>, onDismiss: () -> Unit) {
    val selectAllSelected = mutableStateOf(false)
    val resetSelected = mutableStateOf(false)
    val selectedCategories = mutableStateOf(setOf<Int>())

    Dialog(
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        ),
        onDismissRequest = { onDismiss() }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Categories", style = MaterialTheme.typography.titleLarge)

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(onClick = { onDismiss() }) {
                        Icon(Icons.Outlined.Close, contentDescription = "Close categories dialog")
                    }
                }

                Text("Select Categories to Display", style = MaterialTheme.typography.titleSmall)

                CategoryFilterChipsList(categories, selectedCategories)

                HorizontalDivider()

                SelectionManagement(
                    selectAllSelected,
                    selectedCategories,
                    categories,
                    resetSelected
                )
            }
        }
    }
}

@Composable
private fun CategoryFilterChipsList(
    categories: List<MessageCategory>,
    selectedCategories: MutableState<Set<Int>>
) {
    FlowRow(
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
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

@Composable
private fun SelectionManagement(
    selectAllSelected: MutableState<Boolean>,
    selectedCategories: MutableState<Set<Int>>,
    categories: List<MessageCategory>,
    resetSelected: MutableState<Boolean>
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = selectAllSelected.value,
            onClick = {
                selectAllSelected.value = !selectAllSelected.value
                selectedCategories.value = if (selectAllSelected.value) {
                    categories.map { it.id }.toSet()
                } else {
                    setOf()
                }
            },
            label = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (selectAllSelected.value) {
                        Icon(
                            Icons.Outlined.Check,
                            contentDescription = "Select All selected"
                        )
                    }
                    Text("Select All")

                }
            }
        )
        FilterChip(
            selected = resetSelected.value,
            onClick = {
                resetSelected.value = !resetSelected.value
                selectedCategories.value = setOf()
            },
            label = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (resetSelected.value) {
                        Icon(
                            Icons.Outlined.Check,
                            contentDescription = "Reset selected"
                        )
                    }
                    Text("Reset")
                }
            }
        )
    }
}