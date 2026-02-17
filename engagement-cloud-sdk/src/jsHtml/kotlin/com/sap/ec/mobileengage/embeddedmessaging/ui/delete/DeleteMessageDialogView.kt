package com.sap.ec.mobileengage.embeddedmessaging.ui.delete

import androidx.compose.runtime.Composable
import com.sap.ec.mobileengage.embeddedmessaging.ui.theme.EmbeddedMessagingStyleSheet
import com.sap.ec.mobileengage.embeddedmessaging.ui.translation.LocalStringResources
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun DeleteMessageDialogView(
    onApplyClicked: () -> Unit,
    onDismiss: () -> Unit
) {
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
                Span({
                    classes(EmbeddedMessagingStyleSheet.dialogTitle)
                }) {
                    Text(LocalStringResources.current.deleteMessageDialogTitle)
                }
                Span({
                    classes(EmbeddedMessagingStyleSheet.dialogSubtitle)
                }) {
                    Text(LocalStringResources.current.deleteMessageDialogDescription)
                }
                Div({
                    classes(EmbeddedMessagingStyleSheet.deleteDialogActionsContainer)
                }) {
                    Button({
                        onClick {
                            onDismiss()
                        }
                        classes(
                            EmbeddedMessagingStyleSheet.dialogButton,
                            EmbeddedMessagingStyleSheet.deleteDialogCancelButton
                        )
                    }) {
                        Text(LocalStringResources.current.deleteMessageDialogCancelButtonLabel)
                    }

                    Button({
                        onClick {
                            onApplyClicked()
                        }
                        classes(
                            EmbeddedMessagingStyleSheet.dialogButton,
                            EmbeddedMessagingStyleSheet.deleteDialogApplyButton
                        )
                    }) {
                        Text(LocalStringResources.current.deleteMessageDialogConfirmButtonLabel)
                    }
                }

            }
        }
    }
}