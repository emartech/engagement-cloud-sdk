package com.emarsys.mobileengage.embeddedmessaging.ui.list

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalComposeUiApi::class, ExperimentalJsExport::class)
@JsExport
fun embeddedMessageJs(viewportContainerId: String = "embedded-message-container") {
    ComposeViewport(viewportContainerId = viewportContainerId) {
        EmbeddedMessageContent()
    }
}

@Composable
private fun EmbeddedMessageContent() {
    ListPageView()
}