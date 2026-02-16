package com.emarsys.mobileengage.embeddedmessaging.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.emarsys.mobileengage.embeddedmessaging.ui.item.MessageItemViewModelApi
import com.emarsys.mobileengage.embeddedmessaging.ui.theme.EmbeddedMessagingStyleSheet
import com.emarsys.mobileengage.embeddedmessaging.ui.toReadTagId
import com.emarsys.mobileengage.inapp.view.InlineInAppView
import io.ktor.http.Url
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import web.dom.ElementId
import web.dom.document
import web.intersection.IntersectionObserver
import web.intersection.IntersectionObserverInit

@Composable
fun MessageDetailView(
    viewModel: MessageItemViewModelApi
) {
    var imageDataUrl by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

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
        id(viewModel.id.toReadTagId())
        classes(EmbeddedMessagingStyleSheet.detailViewContainer)
    }) {
        if (!viewModel.isRead) {
            DisposableEffect(Unit) {
                val observer = observeDetailedMessageVisibility(viewModel, scope)
                onDispose {
                    observer.disconnect()
                }
            }
        }
        Div({
            classes(EmbeddedMessagingStyleSheet.detailContent)
        }) {
            H2 {
                Text(viewModel.title)
            }
            Div({
                style {
                    height(500.px)
                }
            }) {
                InlineInAppView(Url("https://www.example.com"), viewModel.trackingInfo)
            }
            imageDataUrl?.let { url ->
                Img(src = url, alt = viewModel.imageAltText ?: "") {
                    classes(EmbeddedMessagingStyleSheet.detailImage)
                }
            }
            P {
                Text(viewModel.lead)
                Div({
                    style {
                        height(500.px)
                    }
                }) {
                    InlineInAppView(Url("https://www.example.com"), viewModel.trackingInfo)
                }
            }
        }
    }
}

private fun observeDetailedMessageVisibility(
    viewModel: MessageItemViewModelApi,
    scope: CoroutineScope
): IntersectionObserver {
    val reportReadAfterMillis = 3000L
    val target = document.getElementById(ElementId(viewModel.id.toReadTagId()))
    var applyReadTagJob: Job? = null

    val observer = IntersectionObserver(
        callback = { entries, _ ->
            if (entries[0].isIntersecting) {
                applyReadTagJob = scope.launch {
                    delay(reportReadAfterMillis)
                    viewModel.tagMessageRead()
                }
            } else {
                applyReadTagJob?.cancel()
                applyReadTagJob = null
            }
        },
        options = js("{}").unsafeCast<IntersectionObserverInit>().apply {
            threshold = arrayOf(0.5)
        }
    )

    if (target != null) {
        observer.observe(target)
    }

    return observer
}

private fun byteArrayToDataUrl(bytes: ByteArray): String {
    val base64 = window.btoa(
        bytes.joinToString("") { (it.toInt() and 0xFF).toChar().toString() }
    )
    return "data:image/jpeg;base64,$base64"
}
