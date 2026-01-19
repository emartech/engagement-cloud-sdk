package com.emarsys.mobileengage.embeddedmessaging.ui.item

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.emarsys.mobileengage.embeddedmessaging.ui.category.SvgIcon
import com.emarsys.mobileengage.embeddedmessaging.ui.theme.EmbeddedMessagingStyleSheet
import com.emarsys.mobileengage.embeddedmessaging.util.asFormattedTimestamp
import kotlinx.browser.window
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

private const val DELETE_ICON_PATH =
    "M7 21q-0.83 0-1.41-0.59t-0.59-1.41V6H4V4h5V3h6v1h5v2h-1v13q0 0.82-0.59 1.41t-1.41 0.59H7Zm10-15H7v13h10V6ZM9 17h2V8H9v9Zm4 0h2V8h-2v9ZM7 6v13-13Z"

@Composable
fun MessageItemView(
    viewModel: MessageItemViewModelApi,
    selectedMessageId: String?,
    onClick: () -> Unit,
    onDeleteClicked: () -> Unit,
    withDeleteIcon: Boolean = true
) {
    var imageDataUrl by remember { mutableStateOf<String?>(null) }
    val hasThumbnailImage = viewModel.imageUrl != null

    val classes = mutableListOf(EmbeddedMessagingStyleSheet.messageItem).apply {
        if (selectedMessageId === viewModel.id) {
            this.add(EmbeddedMessagingStyleSheet.messageItemSelected)
        }
    }

    LaunchedEffect(viewModel.imageUrl) {
        imageDataUrl = viewModel.imageUrl?.let {
            try {
                val imageBytes = viewModel.fetchImage()
                if (imageBytes.isNotEmpty()) {
                    byteArrayToDataUrl(imageBytes)
                } else null
            } catch (_: Exception) {
                null
            }
        }
    }

    Div({
        id("mi-${viewModel.id}")
        classes(classes)
        onClick { onClick() }
    }) {
        if (hasThumbnailImage) {
            imageDataUrl?.let { url ->
                Img(src = url, alt = viewModel.imageAltText ?: "") {
                    classes(EmbeddedMessagingStyleSheet.messageItemImage)
                }
            } ?: LoadingSpinner()

            Div({
                classes(EmbeddedMessagingStyleSheet.messageItemImageSpacer)
            })
        }

        Div({
            classes(
                if (hasThumbnailImage) EmbeddedMessagingStyleSheet.messageItemContent
                else EmbeddedMessagingStyleSheet.messageItemContentNoPadding
            )
        }) {
            Span({
                classes(EmbeddedMessagingStyleSheet.messageItemTitle)
            }) {
                Text(viewModel.title)
            }
            Span({
                classes(EmbeddedMessagingStyleSheet.messageItemLead)
            }) {
                Text(viewModel.lead)
            }
            Span({
                classes(EmbeddedMessagingStyleSheet.messageItemTimestamp)
            }) {
                Text(viewModel.receivedAt.asFormattedTimestamp())
            }
        }

        if (withDeleteIcon) {
            Div({
                classes(EmbeddedMessagingStyleSheet.messageItemMisc)
                onClick { onDeleteClicked() }
            }) {
                SvgIcon(
                    path = DELETE_ICON_PATH,
                    className = EmbeddedMessagingStyleSheet.deleteMessageIcon
                )
            }
        }
    }
}

@Composable
fun LoadingSpinner() {
    Div({
        classes(EmbeddedMessagingStyleSheet.loadingSpinner)
    }) {
        Text("...")
    }
}

private fun byteArrayToDataUrl(bytes: ByteArray): String {
    val base64 = window.btoa(
        bytes.joinToString("") { (it.toInt() and 0xFF).toChar().toString() }
    )
    return "data:image/jpeg;base64,$base64"
}
