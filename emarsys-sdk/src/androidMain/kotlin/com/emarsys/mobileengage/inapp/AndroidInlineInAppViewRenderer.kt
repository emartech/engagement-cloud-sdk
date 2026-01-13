package com.emarsys.mobileengage.inapp

import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

internal class AndroidInlineInAppViewRenderer: InlineInAppViewRendererApi {
    
    @Composable
    override fun Render(holder: WebViewHolder) {
        val androidHolder = holder as AndroidWebViewHolder
        
        AndroidView(
            factory = { 
                androidHolder.webView.apply {
                    (parent as? ViewGroup)?.removeView(this)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
