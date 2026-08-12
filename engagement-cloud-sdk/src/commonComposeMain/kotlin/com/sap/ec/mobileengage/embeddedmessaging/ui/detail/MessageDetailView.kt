package com.sap.ec.mobileengage.embeddedmessaging.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sap.ec.mobileengage.embeddedmessaging.ui.theme.EmbeddedMessagingTheme
import com.sap.ec.mobileengage.inapp.view.InlineInAppView
import io.ktor.http.Url

@Composable
internal fun MessageDetailView(
    richContentUrl: Url,
    trackingInfo: String,
    modifier: Modifier,
    onBack: () -> Unit
) {
    EmbeddedMessagingTheme {
        Column(modifier = modifier.fillMaxSize()) {
            InlineInAppView(
                url = richContentUrl,
                trackingInfo,
                onBack
            )
        }
    }
}
