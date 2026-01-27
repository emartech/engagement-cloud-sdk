package com.emarsys.mobileengage.embeddedmessaging.ui.list.placeholders

import androidx.compose.runtime.Composable
import com.emarsys.mobileengage.embeddedmessaging.ui.theme.EmbeddedMessagingTheme
import org.jetbrains.compose.web.dom.Div


@Composable
fun PlaceholderMessageList() {
    EmbeddedMessagingTheme {
        Div {
            repeat(3) {
                PlaceholderMessageItemView()
            }
        }
    }
}
