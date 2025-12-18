package com.emarsys.mobileengage.embeddedmessaging.ui.item

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.emarsys.mobileengage.embeddedmessaging.ui.theme.EmbeddedMessagingStyleSheet
import kotlinx.browser.window
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Composable
fun MessageItemView(
    viewModel: MessageItemViewModelApi,
    onClick: () -> Unit
) {
    var imageDataUrl by remember { mutableStateOf<String?>(null) }
    val hasThumbnailImage = viewModel.imageUrl != null

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
        classes(EmbeddedMessagingStyleSheet.messageItem)
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
        }

        Span({
            classes(EmbeddedMessagingStyleSheet.messageItemTimestamp)
        }) {
            Text(formatTimestamp(viewModel.receivedAt))
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

@OptIn(ExperimentalTime::class)
private fun formatTimestamp(timestamp: Long): String {
    val now = Clock.System.now()
    val receivedAt = Instant.fromEpochMilliseconds(timestamp)
    val duration = now - receivedAt

    val hours = duration.inWholeHours
    val days = duration.inWholeDays

    return if (days >= 1) {
        "${days}d"
    } else {
        "${hours}h"
    }
}

private fun byteArrayToDataUrl(bytes: ByteArray): String {
    val base64 = window.btoa(
        bytes.joinToString("") { (it.toInt() and 0xFF).toChar().toString() }
    )
    return "data:image/jpeg;base64,$base64"
}
