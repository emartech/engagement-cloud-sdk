package com.emarsys.mobileengage.embeddedmessaging.ui.item

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants
import com.emarsys.mobileengage.embeddedmessaging.ui.category.SvgIcon
import com.emarsys.mobileengage.embeddedmessaging.ui.theme.EmbeddedMessagingStyleSheet
import com.emarsys.mobileengage.embeddedmessaging.util.asFormattedTimestamp
import com.emarsys.util.JsonUtil
import kotlinx.browser.window
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.TagElement
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLElement

private const val DELETE_ICON_PATH =
    "M7 21q-0.83 0-1.41-0.59t-0.59-1.41V6H4V4h5V3h6v1h5v2h-1v13q0 0.82-0.59 1.41t-1.41 0.59H7Zm10-15H7v13h10V6ZM9 17h2V8H9v9Zm4 0h2V8h-2v9ZM7 6v13-13Z"

@Composable
fun MessageItemView(
    viewModel: MessageItemViewModelApi,
    selectedMessageId: String?,
    customMessageItemName: String?,
    onClick: () -> Unit,
    onDeleteClicked: () -> Unit,
    withDeleteIcon: Boolean = true
) {
    var imageDataUrl by remember { mutableStateOf<String?>(null) }
    val hasCustomElementDefined =
        customMessageItemName?.let {
            val isCustomElementFound = window.customElements.get(it) != null
            if (!isCustomElementFound) {
                console.error("CustomMessageItem element with the name $customMessageItemName not found!")
            }
            isCustomElementFound
        } ?: false

    val classes = mutableListOf(EmbeddedMessagingStyleSheet.messageItem).apply {
        if (!hasCustomElementDefined) {
            this.add(EmbeddedMessagingStyleSheet.messageItemHover)

            if (selectedMessageId === viewModel.id) {
                this.add(EmbeddedMessagingStyleSheet.messageItemSelected)
            }
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
        if (hasCustomElementDefined) {
            TagElement<HTMLElement>(customMessageItemName, applyAttrs = {
                style {
                    display(DisplayStyle.Block)
                    width(EmbeddedMessagingUiConstants.MAX_WIDTH)
                }
                attr("title", viewModel.title)
                attr("lead", viewModel.lead)
                viewModel.imageUrl?.let { attr("image", it) }
                viewModel.imageAltText?.let { attr("image-alt-text", it) }
                attr("is-selected", (selectedMessageId === viewModel.id).toString())
                attr("received-at", viewModel.receivedAt.asFormattedTimestamp())
                attr("is-not-opened", viewModel.isNotOpened.toString())
                attr("is-pinned", viewModel.isPinned.toString())
                attr("is-deleted", viewModel.isDeleted.toString())
                attr("categories", JsonUtil.json.encodeToString(viewModel.categories))
            }) {}
        } else {
            MessageItemCore(viewModel, imageDataUrl)
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
private fun MessageItemCore(viewModel: MessageItemViewModelApi, imageDataUrl: String?) {
    val titleClasses = mutableListOf(EmbeddedMessagingStyleSheet.messageItemTitle).apply {
        if (viewModel.isNotOpened) {
            this.add(EmbeddedMessagingStyleSheet.unopened)
        }
    }
    val leadClasses = mutableListOf(EmbeddedMessagingStyleSheet.messageItemLead).apply {
        if (viewModel.isNotOpened) {
            this.add(EmbeddedMessagingStyleSheet.unopened)
        }
    }
    val hasThumbnailImage = viewModel.imageUrl != null
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
            classes(titleClasses)
        }) {
            Text(viewModel.title)
        }
        Span({
            classes(leadClasses)
        }) {
            Text(viewModel.lead)
        }
        Span({
            classes(EmbeddedMessagingStyleSheet.messageItemTimestamp)
        }) {
            Text(viewModel.receivedAt.asFormattedTimestamp())
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
