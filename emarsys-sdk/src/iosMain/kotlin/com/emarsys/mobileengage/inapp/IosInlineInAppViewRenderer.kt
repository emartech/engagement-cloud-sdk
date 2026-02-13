package com.emarsys.mobileengage.inapp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import com.emarsys.mobileengage.inapp.presentation.InlineInAppViewRendererApi
import com.emarsys.mobileengage.inapp.webview.WebViewHolder
import kotlinx.cinterop.ExperimentalForeignApi

internal class IosInlineInAppViewRenderer: InlineInAppViewRendererApi {

    @OptIn(ExperimentalForeignApi::class)
    @Composable
    override fun Render(holder: WebViewHolder) {
        val iosHolder = holder as IosWebViewHolder

        UIKitView(
            factory = { iosHolder.webView },
            modifier = Modifier.fillMaxSize()
        )
    }
}
