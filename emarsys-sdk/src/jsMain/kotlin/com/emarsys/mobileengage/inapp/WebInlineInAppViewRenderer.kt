package com.emarsys.mobileengage.inapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.emarsys.mobileengage.inapp.presentation.InlineInAppViewRendererApi
import com.emarsys.mobileengage.inapp.webview.WebViewHolder
import kotlinx.browser.document
import org.w3c.dom.HTMLElement

internal class WebInlineInAppViewRenderer : InlineInAppViewRendererApi {
    
    @Composable
    override fun Render(holder: WebViewHolder) {
        val webViewElement = holder.asDynamic().webView as? HTMLElement
        val containerId = remember { "ems-inline-inapp-container-${js("Date.now()")}" }

        //TODO: Why DisposableEffect why not LaunchedEffect?
        DisposableEffect(holder) {
            webViewElement?.let { webView ->
                val container = document.createElement("div") as HTMLElement
                container.id = containerId
                container.style.width = "100%"
                container.style.height = "100%"
                container.appendChild(webView)
                document.body?.appendChild(container)
            }
            
            onDispose {
                document.getElementById(containerId)?.remove()
            }
        }
    }
}
