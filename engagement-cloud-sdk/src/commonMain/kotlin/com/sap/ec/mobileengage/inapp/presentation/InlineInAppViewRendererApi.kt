package com.sap.ec.mobileengage.inapp.presentation

import androidx.compose.runtime.Composable
import com.sap.ec.mobileengage.inapp.webview.WebViewHolder

internal interface InlineInAppViewRendererApi {


    @Composable
    fun Render(holder: WebViewHolder)
}
