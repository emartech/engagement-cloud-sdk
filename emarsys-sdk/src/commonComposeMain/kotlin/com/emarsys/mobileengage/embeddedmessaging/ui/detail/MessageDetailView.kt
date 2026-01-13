package com.emarsys.mobileengage.embeddedmessaging.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.Dimensions.DEFAULT_PADDING
import com.emarsys.mobileengage.embeddedmessaging.ui.item.MessageItemViewModelApi
import com.emarsys.mobileengage.embeddedmessaging.ui.theme.EmbeddedMessagingTheme
import com.emarsys.mobileengage.inapp.InlineInAppView
import io.ktor.http.Url

@Composable
fun MessageDetailView(
    messageViewModel: MessageItemViewModelApi,
    showBackButton: Boolean,
    onBack: () -> Unit
) {
    EmbeddedMessagingTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            if (!showBackButton) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
            Text(
                text = "Message Title: ${messageViewModel.title}",
                modifier = Modifier.padding(DEFAULT_PADDING)
            )

            val url = remember { //TODO: it should be changed
                Url("https://www.example.com")
            }

            InlineInAppView(url = url)
        }
    }
}
