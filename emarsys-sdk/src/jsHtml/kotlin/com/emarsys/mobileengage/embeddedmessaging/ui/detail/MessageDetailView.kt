package com.emarsys.mobileengage.embeddedmessaging.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.emarsys.mobileengage.embeddedmessaging.ui.item.MessageItemViewModelApi
import com.emarsys.mobileengage.embeddedmessaging.ui.theme.EmbeddedMessagingStyleSheet
import kotlinx.browser.window
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

@Composable
fun MessageDetailView(
    viewModel: MessageItemViewModelApi,
    isSplitView: Boolean,
    onBack: () -> Unit
) {
    var imageDataUrl by remember { mutableStateOf<String?>(null) }

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
        classes(EmbeddedMessagingStyleSheet.detailViewContainer)
    }) {
        if (!isSplitView) {
            Button({
                onClick { onBack() }
                classes(EmbeddedMessagingStyleSheet.detailBackButton)
            }) {
                Text("← Back")
            }
        }

        Div({
            classes(EmbeddedMessagingStyleSheet.detailContent)
        }) {
            H2 {
                Text(viewModel.title)
            }
            imageDataUrl?.let { url ->
                Img(src = url, alt = viewModel.imageAltText ?: "") {
                    classes(EmbeddedMessagingStyleSheet.detailImage)
                }
            }
            P {
                Text(viewModel.lead)
            }
        }
    }

}

private fun byteArrayToDataUrl(bytes: ByteArray): String {
    val base64 = window.btoa(
        bytes.joinToString("") { (it.toInt() and 0xFF).toChar().toString() }
    )
    return "data:image/jpeg;base64,$base64"
}
