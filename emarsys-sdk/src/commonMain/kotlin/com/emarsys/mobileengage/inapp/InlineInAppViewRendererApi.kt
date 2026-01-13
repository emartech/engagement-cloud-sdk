package com.emarsys.mobileengage.inapp

import androidx.compose.runtime.Composable

internal interface InlineInAppViewRendererApi {


    @Composable
    fun Render(holder: WebViewHolder)
}
