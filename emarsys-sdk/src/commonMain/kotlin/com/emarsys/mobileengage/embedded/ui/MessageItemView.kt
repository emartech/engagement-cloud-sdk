package com.emarsys.mobileengage.embedded.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emarsys.networking.clients.embedded.messaging.model.EmbeddedMessage


@Composable
fun MessageItemView(message: EmbeddedMessage) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(8.dp)
    ) {
        Text("Image")

        Spacer(modifier = Modifier.padding(8.dp))

        Column {
            Text(text = message.title)
            Text(text = message.lead)
        }
    }
}
