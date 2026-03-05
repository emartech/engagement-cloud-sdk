package com.sap.ec.mobileengage.embeddedmessaging.ui.item

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
import androidx.compose.ui.unit.sp
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.Dimensions.BODY_SMALL_WEIGHT
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.Dimensions.BUTTON_LABEL_FONT_WEIGHT
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.Dimensions.MEDIUM_PADDING
import com.sap.ec.mobileengage.embeddedmessaging.ui.translation.LocalStringResources


@Composable
fun DeleteMessageItemConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    MaterialTheme {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = LocalStringResources.current.deleteMessageDialogTitle,
                )
            },
            text = {
                Text(
                    text = LocalStringResources.current.deleteMessageDialogDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = BODY_SMALL_WEIGHT,
                    letterSpacing = 0.25.sp,
                    modifier = Modifier.padding(top = MEDIUM_PADDING, bottom = MEDIUM_PADDING)
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
                    Text(
                        text = LocalStringResources.current.deleteMessageDialogConfirmButtonLabel,
                        fontWeight = BUTTON_LABEL_FONT_WEIGHT,
                        letterSpacing = 0.1.sp,
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss
                ) {
                    Text(
                        text = LocalStringResources.current.deleteMessageDialogCancelButtonLabel,
                        fontWeight = BUTTON_LABEL_FONT_WEIGHT,
                        letterSpacing = 0.1.sp,
                    )
                }
            },
            shape = MaterialTheme.shapes.large
        )
    }
}