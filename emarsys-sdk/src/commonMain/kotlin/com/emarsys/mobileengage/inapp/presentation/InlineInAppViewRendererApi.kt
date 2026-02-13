package com.emarsys.mobileengage.inapp.presentation

import androidx.compose.runtime.Composable
import com.emarsys.mobileengage.inapp.webview.WebViewHolder

internal interface InlineInAppViewRendererApi {


    @Composable
    fun Render(holder: WebViewHolder)
}
