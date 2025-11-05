package com.emarsys.mobileengage.embeddedmessaging.ui.item

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.emarsys.networking.clients.embedded.messaging.model.EmbeddedMessage
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant


@Composable
fun MessageItemView(message: EmbeddedMessage, viewModel: MessageItemViewModel) {
    val imageBitmap = remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(message.imageUrl) {
        imageBitmap.value = viewModel.fetchImage(message.imageUrl)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(8.dp)
    ) {
        imageBitmap.value?.let {
            Image(it, contentScale = ContentScale.Crop, contentDescription = null)
        } ?: LoadingSpinner()

        Spacer(modifier = Modifier.padding(8.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
        ) {
            Text(text = message.title)
            Text(text = message.lead, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }

        Text(formatTimestamp(message.receivedAt))
    }
}

@Composable
fun LoadingSpinner() {
    CircularProgressIndicator(
        modifier = Modifier.width(64.dp)
    )
}

private fun formatTimestamp(timestamp: Long): String {
    val now = Clock.System.now()
    val receivedAt = Instant.fromEpochMilliseconds(timestamp)
    val duration = now - receivedAt

    val hours = duration.inWholeHours
    val days = duration.inWholeDays

    return if (days >= 1) {
        "${days}d"
    } else {
        "${hours}h"
    }
}
