package com.emarsys.mobileengage.embeddedmessaging.ui.item

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.Dimensions.LARGE_PADDING
import com.emarsys.mobileengage.embeddedmessaging.ui.translation.LocalStringResources


@Composable
fun DeleteMessageItemConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    MaterialTheme {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(LocalStringResources.current.deleteMessageDialogTitle) },
            text = {
                Text(
                    text = LocalStringResources.current.deleteMessageDialogDescription,
                    modifier = Modifier.padding(top = LARGE_PADDING, bottom = LARGE_PADDING)
                )
            },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    colors = ButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = ButtonDefaults.buttonColors().disabledContainerColor,
                        disabledContentColor = ButtonDefaults.buttonColors().disabledContentColor
                    ),
                    shape = MaterialTheme.shapes.large
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
            },
            shape = MaterialTheme.shapes.large
        )
    }
}