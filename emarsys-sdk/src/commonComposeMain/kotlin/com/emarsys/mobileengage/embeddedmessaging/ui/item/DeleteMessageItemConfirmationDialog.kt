package com.emarsys.mobileengage.embeddedmessaging.ui.item

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.emarsys.mobileengage.embeddedmessaging.ui.translation.LocalStringResources


@Composable
internal fun DeleteMessageItemConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(LocalStringResources.current.deleteMessageDialogTitle) },
        text = { Text(LocalStringResources.current.deleteMessageDialogDescription) },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(LocalStringResources.current.deleteMessageDialogConfirmButtonLabel)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(LocalStringResources.current.deleteMessageDialogCancelButtonLabel)
            }
        }
    )
}