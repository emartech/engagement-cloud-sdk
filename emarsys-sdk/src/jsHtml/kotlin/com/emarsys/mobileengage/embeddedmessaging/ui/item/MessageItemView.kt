package com.emarsys.mobileengage.embeddedmessaging.ui.item

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.DEFAULT_PADDING
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.MESSAGE_ITEM_IMAGE_SIZE
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.ZERO_PADDING
import com.emarsys.mobileengage.embeddedmessaging.ui.theme.EmbeddedMessagingTheme
import kotlinx.browser.window
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flex
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Composable
fun MessageItemView(viewModel: MessageItemViewModelApi) {
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

    EmbeddedMessagingTheme {
        Div({
            style {
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Row)
                alignItems(AlignItems.Center)
                padding(DEFAULT_PADDING)
            }
        }) {
            if (hasThumbnailImage) {
                if (imageDataUrl != null) {
                    Img(src = imageDataUrl!!, alt = viewModel.imageAltText ?: "") {
                        style {
                            width(MESSAGE_ITEM_IMAGE_SIZE)
                            height(MESSAGE_ITEM_IMAGE_SIZE)
                            property("object-fit", "cover")
                        }
                    }
                } else {
                    LoadingSpinner()
                }

                Div({ style { padding(DEFAULT_PADDING) } })
            }

            Div({
                style {
                    flex(1)
                    padding(if (hasThumbnailImage) DEFAULT_PADDING else ZERO_PADDING)
                }
            }) {
                Span({
                    style {
                        fontSize(16.px)
                        property("color", "var(--color-on-surface)")
                        display(DisplayStyle.Block)
                    }
                }) {
                    Text(viewModel.title)
                }
                Span({
                    style {
                        fontSize(16.px)
                        property("color", "var(--color-on-surface)")
                        property("overflow", "hidden")
                        property("text-overflow", "ellipsis")
                        property("white-space", "nowrap")
                        display(DisplayStyle.Block)
                    }
                }) {
                    Text(viewModel.lead)
                }
            }

            Span({
                style {
                    fontSize(16.px)
                    property("color", "var(--color-on-surface)")
                }
            }) {
                Text(formatTimestamp(viewModel.receivedAt))
            }
        }
    }
}

@Composable
fun LoadingSpinner() {
    Div({
        style {
            width(MESSAGE_ITEM_IMAGE_SIZE)
            height(MESSAGE_ITEM_IMAGE_SIZE)
            display(DisplayStyle.Flex)
            alignItems(AlignItems.Center)
            justifyContent(JustifyContent.Center)
            property("color", "var(--color-on-surface)")
        }
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
    // Convert ByteArray to base64 data URL
    val base64 = window.btoa(
        bytes.joinToString("") { (it.toInt() and 0xFF).toChar().toString() }
    )
    return "data:image/jpeg;base64,$base64"
}
