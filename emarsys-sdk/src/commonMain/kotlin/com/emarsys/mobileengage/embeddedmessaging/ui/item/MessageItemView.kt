package com.emarsys.mobileengage.embeddedmessaging.ui.item

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.emarsys.mobileengage.embeddedmessaging.ui.theme.EmbeddedMessagingTheme
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


@Composable
fun MessageItemView(viewModel: MessageItemViewModelApi) {
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    val hasThumbnailImage = viewModel.imageUrl != null

    LaunchedEffect(viewModel.imageUrl) {
        imageBitmap = viewModel.imageUrl?.let {
            try {
                viewModel.fetchImage().takeIf {
                    it.width > 0 && it.height > 0
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    EmbeddedMessagingTheme {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            if (hasThumbnailImage) {
                imageBitmap?.let {
                    Image(
                        bitmap = it,
                        contentDescription = viewModel.imageAltText,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(54.dp)
                    )
                } ?: LoadingSpinner()
                Spacer(modifier = Modifier.padding(8.dp))
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(if (hasThumbnailImage) 8.dp else 0.dp)
            ) {
                Text(text = viewModel.title)
                Text(text = viewModel.lead, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            Text(formatTimestamp(viewModel.receivedAt))
        }
    }
}

@Composable
fun LoadingSpinner() {
    EmbeddedMessagingTheme {
        CircularProgressIndicator(
            modifier = Modifier.width(54.dp)
        )
    }
}

@OptIn(ExperimentalTime::class)
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
