package com.sap.ec.mobileengage.embeddedmessaging.ui.item

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.SWIPE_THRESHOLD_PERCENTAGE
import com.sap.ec.mobileengage.embeddedmessaging.ui.category.SvgIcon
import com.sap.ec.mobileengage.embeddedmessaging.ui.theme.EmbeddedMessagingStyleSheet
import com.sap.ec.mobileengage.embeddedmessaging.util.asFormattedTimestamp
import com.sap.ec.util.JsonUtil
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
import org.w3c.dom.TouchEvent
import org.w3c.dom.events.EventListener
import web.console.console

private const val DELETE_ICON_PATH =
    "M7 21q-0.83 0-1.41-0.59t-0.59-1.41V6H4V4h5V3h6v1h5v2h-1v13q0 0.82-0.59 1.41t-1.41 0.59H7Zm10-15H7v13h10V6ZM9 17h2V8H9v9Zm4 0h2V8h-2v9ZM7 6v13-13Z"

@Composable
fun MessageItemView(
    viewModel: MessageItemViewModelApi,
    selectedMessageId: String?,
    customMessageItemName: String?,
    onClick: () -> Unit,
    onDeleteClicked: () -> Unit,
    withDeleteIcon: Boolean = true,
    withSwipeGesture: Boolean = false
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
                    byteArrayToDataUrl(
                        imageBytes
                    )
                } else null
            } catch (_: Exception) {
                null
            }
        }
    }

    if (withSwipeGesture) {
        SwipeableMessageItem(
            viewModel = viewModel,
            selectedMessageId = selectedMessageId,
            customMessageItemName = customMessageItemName,
            hasCustomElementDefined = hasCustomElementDefined,
            imageDataUrl = imageDataUrl,
            classes = classes,
            onClick = onClick,
            onDeleteClicked = onDeleteClicked
        )
    } else {
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
                ECMessageItem(
                    viewModel,
                    imageDataUrl
                )
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
}

@Composable
private fun SwipeableMessageItem(
    viewModel: MessageItemViewModelApi,
    selectedMessageId: String?,
    customMessageItemName: String?,
    hasCustomElementDefined: Boolean,
    imageDataUrl: String?,
    classes: List<String>,
    onClick: () -> Unit,
    onDeleteClicked: () -> Unit
) {
    var swipeElementRef by remember { mutableStateOf<HTMLElement?>(null) }
    var swipeContainerRef by remember { mutableStateOf<HTMLElement?>(null) }

    Div({
        id("mi-${viewModel.id}")
        classes(EmbeddedMessagingStyleSheet.swipeContainer)
        ref { element ->
            swipeContainerRef = element
            onDispose { }
        }
    }) {
        Div({
            classes(EmbeddedMessagingStyleSheet.swipeDeleteBackground)
        }) {
            SvgIcon(
                path = DELETE_ICON_PATH,
                className = EmbeddedMessagingStyleSheet.swipeDeleteIcon
            )
        }

        Div({
            classes(classes + EmbeddedMessagingStyleSheet.swipeContent)
            onClick { onClick() }
            ref { element ->
                swipeElementRef = element
                onDispose { }
            }
        }) {
            if (hasCustomElementDefined && customMessageItemName != null) {
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
                ECMessageItem(
                    viewModel,
                    imageDataUrl
                )
            }
        }

        DisposableEffect(swipeContainerRef) {
            val swipeElement = swipeElementRef
            val swipeContainer = swipeContainerRef

            if (swipeElement != null && swipeContainer != null) {
                var startX = 0.0
                var currentX = 0.0
                var isSwiping = false
                var hasTriggeredDelete = false

                val onTouchStart = EventListener { event ->
                    val touchEvent = event as TouchEvent
                    if (touchEvent.touches.length > 0) {
                        val touch = touchEvent.touches.item(0)
                        if (touch != null) {
                            startX = touch.clientX.toDouble()
                            currentX = startX
                            isSwiping = true
                            hasTriggeredDelete = false
                            swipeElement.style.transition = "none"
                        }
                    }
                }

                val onTouchMove = EventListener { event ->
                    if (!isSwiping) return@EventListener
                    val touchEvent = event as TouchEvent
                    if (touchEvent.touches.length > 0) {
                        val touch = touchEvent.touches.item(0)
                        if (touch != null) {
                            currentX = touch.clientX.toDouble()
                            val deltaX = currentX - startX

                            if (deltaX < 0) {
                                event.preventDefault()
                                val containerWidth = swipeContainer.offsetWidth
                                val swipeThreshold = containerWidth * SWIPE_THRESHOLD_PERCENTAGE

                                val clampedDelta = kotlin.math.max(deltaX, -swipeThreshold * 1.5)
                                swipeElement.style.transform = "translateX(${clampedDelta}px)"

                                if (kotlin.math.abs(deltaX) >= swipeThreshold && !hasTriggeredDelete) {
                                    hasTriggeredDelete = true
                                }
                            }
                        }
                    }
                }

                val onTouchEnd = EventListener {
                    if (!isSwiping) return@EventListener
                    isSwiping = false
                    swipeElement.style.transition = "transform 0.3s cubic-bezier(0.4, 0.0, 0.2, 1)"

                    if (hasTriggeredDelete) {
                        onDeleteClicked()
                    }

                    swipeElement.style.transform = "translateX(0)"
                }

                println("TAG - swiperContainer: $swipeContainer")
                swipeContainer.addEventListener(
                    "touchstart",
                    onTouchStart,
                    options = js("{passive:false}")
                )
                swipeContainer.addEventListener(
                    "touchmove",
                    onTouchMove,
                    options = js("{passive:false}")
                )
                swipeContainer.addEventListener(
                    "touchend",
                    onTouchEnd,
                    options = js("{passive:false}")
                )
                swipeContainer.addEventListener(
                    "touchcancel",
                    onTouchEnd,
                    options = js("{passive:false}")
                )

                onDispose {
                    swipeContainer.removeEventListener(
                        "touchstart",
                        onTouchStart,
                        options = js("{passive:false}")
                    )
                    swipeContainer.removeEventListener(
                        "touchmove",
                        onTouchMove,
                        options = js("{passive:false}")
                    )
                    swipeContainer.removeEventListener(
                        "touchend",
                        onTouchEnd,
                        options = js("{passive:false}")
                    )
                    swipeContainer.removeEventListener(
                        "touchcancel",
                        onTouchEnd,
                        options = js("{passive:false}")
                    )
                }
            } else {
                onDispose { }
            }
        }
    }
}

@Composable
private fun ECMessageItem(viewModel: MessageItemViewModelApi, imageDataUrl: String?) {
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
