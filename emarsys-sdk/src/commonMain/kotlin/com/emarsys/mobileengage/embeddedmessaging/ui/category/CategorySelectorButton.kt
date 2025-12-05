package com.emarsys.mobileengage.embeddedmessaging.ui.category

import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Dimensions.FLOATING_ACTION_BUTTON_SIZE
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
            modifier = Modifier.height(FLOATING_ACTION_BUTTON_SIZE),
            icon = {
                Icon(
                    if (isCategorySelectionActive) Icons.Filled.FilterAlt else Icons.Outlined.FilterAlt,
                    LocalStringResources.current.categoriesFilterIconAltText
                )
            },
            text = { Text(LocalStringResources.current.categoriesFilterButtonLabel) },
            containerColor = if (isCategorySelectionActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isCategorySelectionActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            shape = MaterialTheme.shapes.small
        )
    }
}