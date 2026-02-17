package com.sap.ec.mobileengage.embeddedmessaging.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.sap.ec.mobileengage.embeddedmessaging.ui.item.MessageItemViewModelApi
import com.sap.ec.mobileengage.embeddedmessaging.ui.theme.EmbeddedMessagingTheme
import com.sap.ec.mobileengage.inapp.view.InlineInAppView

@Composable
fun MessageDetailView(
    messageViewModel: MessageItemViewModelApi,
    onBack: () -> Unit,
    modifier: Modifier
) {
    EmbeddedMessagingTheme {
        Column(modifier = modifier.fillMaxSize()) {
            val url = remember {
                messageViewModel.richContentUrl
            }

            if (url != null) {
                InlineInAppView(
                    url = url,
                    messageViewModel.trackingInfo,
                    onBack
                )
            } else {
                //TODO: error handling
            }
        }
    }
}
