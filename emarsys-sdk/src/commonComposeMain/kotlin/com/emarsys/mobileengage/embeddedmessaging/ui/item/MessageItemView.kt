package com.emarsys.mobileengage.embeddedmessaging.ui.item

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.Dimensions.DEFAULT_PADDING
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.Dimensions.MESSAGE_ITEM_IMAGE_SIZE
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.Dimensions.ZERO_PADDING
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.Shapes.ZERO_CORNER_RADIUS
import com.emarsys.mobileengage.embeddedmessaging.ui.theme.EmbeddedMessagingTheme
import com.emarsys.mobileengage.embeddedmessaging.ui.theme.LocalDesignValues
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


@Composable
fun MessageItemView(
    viewModel: MessageItemViewModelApi,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    val hasThumbnailImage = viewModel.imageUrl != null

    LaunchedEffect(viewModel.imageUrl) {
        imageBitmap = viewModel.imageUrl?.let {
            try {
                viewModel.fetchImage().decodeToImageBitmap().takeIf {
                    it.width > 0 && it.height > 0
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    EmbeddedMessagingTheme {
        Card(
            shape = RoundedCornerShape(LocalDesignValues.current.messageItemCardCornerRadius),
            elevation = CardDefaults.cardElevation(LocalDesignValues.current.messageItemCardElevation),
            colors = if (isSelected) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            else CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.clickable(onClick = { onClick() })
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(DEFAULT_PADDING)
            ) {
                if (hasThumbnailImage) {
                    imageBitmap?.let {
                        Image(
                            bitmap = it,
                            contentDescription = viewModel.imageAltText,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(MESSAGE_ITEM_IMAGE_SIZE)
                                .clip(imageClipShape())
                        )
                    } ?: LoadingSpinner()

                    Spacer(modifier = Modifier.padding(DEFAULT_PADDING))
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(if (hasThumbnailImage) DEFAULT_PADDING else ZERO_PADDING)
                ) {
                    Text(
                        text = viewModel.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (viewModel.isUnread) FontWeight.Bold else FontWeight.Normal
                    )
                    Text(
                        text = viewModel.lead,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (viewModel.isUnread) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    formatTimestamp(viewModel.receivedAt),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun imageClipShape(): RoundedCornerShape {
    val fallbackShape = RoundedCornerShape(ZERO_CORNER_RADIUS)
    return when (LocalDesignValues.current.messageItemClipShape) {
        "Rectangle" -> fallbackShape
        "Circle" -> CircleShape
        "Rounded" -> RoundedCornerShape(LocalDesignValues.current.messageItemImageCornerRadius)
        "Custom" -> {
            val customShape = LocalDesignValues.current.messageItemCustomShape
            customShape?.let {
                RoundedCornerShape(
                    topStart = customShape.topStart.dp,
                    topEnd = customShape.topEnd.dp,
                    bottomStart = customShape.bottomStart.dp,
                    bottomEnd = customShape.bottomEnd.dp
                )
            } ?: fallbackShape
        }

        else -> fallbackShape
    }
}

@Composable
fun LoadingSpinner() {
    EmbeddedMessagingTheme {
        CircularProgressIndicator(
            modifier = Modifier.size(MESSAGE_ITEM_IMAGE_SIZE)
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
