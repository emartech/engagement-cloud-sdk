package com.sap.ec.mobileengage.embeddedmessaging.ui.list.placeholders

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.CardDefaults.cardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.unit.dp
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.Dimensions.DEFAULT_PADDING
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.Dimensions.MESSAGE_ITEM_IMAGE_SIZE
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.Dimensions.SMALL_PADDING
import com.sap.ec.mobileengage.embeddedmessaging.ui.theme.EmbeddedMessagingTheme
import com.sap.ec.mobileengage.embeddedmessaging.ui.theme.LocalDesignValues


@Composable
fun PlaceholderMessageList() {
    EmbeddedMessagingTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(DEFAULT_PADDING),
            verticalArrangement = Arrangement.spacedBy(LocalDesignValues.current.listItemSpacing)
        ) {
            repeat(3) {
                PlaceholderMessageItem()
            }
        }
    }
}

@Composable
private fun PlaceholderMessageItem() {
    val placeholderColor = MaterialTheme.colorScheme.surfaceContainer
    EmbeddedMessagingTheme {
        Card(
            shape = RoundedCornerShape(
                LocalDesignValues.current.messageItemCardCornerRadius
            ),
            elevation = cardElevation(
                LocalDesignValues.current.messageItemCardElevation
            ),
            colors = cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(DEFAULT_PADDING)
            ) {
                Box(
                    modifier = Modifier
                        .size(MESSAGE_ITEM_IMAGE_SIZE)
                        .background(
                            placeholderColor,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .shimmerEffect()
                )

                Spacer(
                    modifier = Modifier.padding(DEFAULT_PADDING)
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(DEFAULT_PADDING)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .padding(vertical = SMALL_PADDING)
                            .background(
                                placeholderColor,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .size(height = 16.dp, width = 100.dp)
                            .shimmerEffect()
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(vertical = SMALL_PADDING)
                            .background(
                                placeholderColor,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .size(height = 14.dp, width = 150.dp)
                            .shimmerEffect()
                    )

                    Box(
                        modifier = Modifier
                            .padding(vertical = SMALL_PADDING)
                            .background(
                                placeholderColor,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .size(height = 14.dp, width = 30.dp)
                            .shimmerEffect()
                    )
                }
            }
        }
    }
}

@Composable
private fun Modifier.shimmerEffect(): Modifier {
    val shimmer = Shimmer(
        highlightColor = MaterialTheme.colorScheme.surfaceTint.copy(alpha = 0.6f),
        intensity = 0.2f,
        dropOff = 0.5f,
        tilt = 20f
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = shimmer.animationSpec,
        label = "shimmer_progress"
    )

    return this.drawWithContent {
        drawContent()
        drawRect(shimmer.brush(progress, size), alpha = shimmer.alpha(progress))
    }
}