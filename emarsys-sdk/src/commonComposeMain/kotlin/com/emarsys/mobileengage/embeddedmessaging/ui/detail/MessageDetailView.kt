package com.emarsys.mobileengage.embeddedmessaging.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.emarsys.mobileengage.embeddedmessaging.ui.item.MessageItemViewModelApi
import com.emarsys.mobileengage.embeddedmessaging.ui.theme.EmbeddedMessagingTheme
import com.emarsys.mobileengage.inapp.InlineInAppView

@Composable
fun MessageDetailView(
    messageViewModel: MessageItemViewModelApi,
    onBack: () -> Unit,
) {
    EmbeddedMessagingTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            val url = remember {
                messageViewModel.richContentUrl
            }

            if (url != null) {
                InlineInAppView(url = url)
            } else {
                //TODO: error handling
            }
        }
    }
}
