package com.emarsys.mobileengage.embeddedmessaging.ui.category

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.emarsys.mobileengage.embeddedmessaging.ui.theme.EmbeddedMessagingTheme
import com.emarsys.mobileengage.embeddedmessaging.ui.translation.LocalStringResources


@Composable
fun CategorySelectorButton(
    isCategorySelectionActive: Boolean,
    onClick: () -> Unit
) {
    EmbeddedMessagingTheme {
        ExtendedFloatingActionButton(
            onClick = onClick,
            icon = {
                Icon(
                    if (isCategorySelectionActive) Icons.Filled.FilterAlt else Icons.Outlined.FilterAlt,
                    LocalStringResources.current.categoriesFilterIconAltText
                )
            },
            text = { Text(LocalStringResources.current.categoriesFilterButtonLabel) },
            containerColor = if (isCategorySelectionActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
            contentColor = if (isCategorySelectionActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary,
            shape = MaterialTheme.shapes.small
        )
    }
}