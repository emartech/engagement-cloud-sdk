package com.sap.ec.mobileengage.embeddedmessaging.ui.tab

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sap.ec.mobileengage.embeddedmessaging.ui.theme.EmbeddedMessagingTheme


@Composable
internal fun FilterByReadStateTabs(
    selectedTabIndex: Int,
    allMessagesText: String,
    unreadMessagesText: String,
    onAllMessagesClick: () -> Unit,
    onUnreadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    EmbeddedMessagingTheme {
        val primaryColor = MaterialTheme.colorScheme.primary
        val onSurfaceColor = MaterialTheme.colorScheme.onSurface
        val density = LocalDensity.current

        var allMessagesWidth by remember { mutableStateOf(0.dp) }
        var unreadMessagesWidth by remember { mutableStateOf(0.dp) }

        val indicatorOffset by animateDpAsState(
            targetValue = if (selectedTabIndex == 0) 0.dp else allMessagesWidth,
            animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
            label = "indicatorOffset"
        )

        val indicatorWidth = if (selectedTabIndex == 0) allMessagesWidth else unreadMessagesWidth

        Box(modifier = modifier.wrapContentSize()) {
            Row {
                TextButton(
                    onClick = onAllMessagesClick,
                    modifier = Modifier.onSizeChanged { size ->
                        allMessagesWidth = with(density) { size.width.toDp() }
                    }
                ) {
                    Text(
                        text = allMessagesText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (selectedTabIndex == 0) primaryColor else onSurfaceColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                TextButton(
                    onClick = onUnreadClick,
                    modifier = Modifier.onSizeChanged { size ->
                        unreadMessagesWidth = with(density) { size.width.toDp() }
                    }
                ) {
                    Text(
                        text = unreadMessagesText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (selectedTabIndex == 1) primaryColor else onSurfaceColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (indicatorWidth > 0.dp) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = indicatorOffset)
                        .width(indicatorWidth)
                        .drawBehind {
                            val strokeWidth = 3.dp.toPx()
                            val y = size.height - strokeWidth / 2
                            val indicatorWidth = size.width * 0.6f
                            val startX = (size.width - indicatorWidth) / 2
                            drawLine(
                                color = primaryColor,
                                start = Offset(startX, y),
                                end = Offset(startX + indicatorWidth, y),
                                strokeWidth = strokeWidth
                            )
                        }
                )
            }
        }
    }
}