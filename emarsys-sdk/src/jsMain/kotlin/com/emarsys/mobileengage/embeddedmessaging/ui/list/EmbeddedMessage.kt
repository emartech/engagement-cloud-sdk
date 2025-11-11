package com.emarsys.mobileengage.embeddedmessaging.ui.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.emarsys.core.util.DownloaderApi
import com.emarsys.di.SdkKoinIsolationContext.koin
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
    val imageDownloader = koin.get<DownloaderApi>()
    val coroutineScope = rememberCoroutineScope()

    ListPageView(
        ListPageViewModel(
            ListPageModel(),
            imageDownloader,
            coroutineScope = coroutineScope
        )
    )
}