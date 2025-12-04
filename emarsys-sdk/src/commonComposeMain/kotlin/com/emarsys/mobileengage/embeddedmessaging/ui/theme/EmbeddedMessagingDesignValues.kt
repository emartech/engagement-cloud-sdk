package com.emarsys.mobileengage.embeddedmessaging.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.Dimensions.DEFAULT_ELEVATION
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.Dimensions.DEFAULT_PADDING
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.Dimensions.ZERO_ELEVATION
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.Dimensions.ZERO_SPACING
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Image.DEFAULT_IMAGE_CLIP_SHAPE
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.Shapes.ZERO_CORNER_RADIUS
import com.emarsys.networking.clients.embedded.messaging.model.ShapeCornerRadius

@Immutable
internal data class EmbeddedMessagingDesignValues(
    val messageItemMargin: Dp = DEFAULT_PADDING,
    val messageItemElevation: Dp = DEFAULT_ELEVATION,

    val buttonElevation: Dp = DEFAULT_ELEVATION,

    val listContentPadding: Dp = DEFAULT_PADDING,
    val listItemSpacing: Dp = ZERO_SPACING,

    val compactOverlayWidth: Dp = DEFAULT_PADDING,
    val compactOverlayMaxHeight: Dp = DEFAULT_PADDING,
    val compactOverlayCornerRadius: Dp = DEFAULT_PADDING,
    val compactOverlayElevation: Dp = DEFAULT_ELEVATION,

    val emptyStateImageUrl: String? = null,
    val messageItemCardCornerRadius: Dp = ZERO_CORNER_RADIUS,
    val messageItemCardElevation: Dp= ZERO_ELEVATION,
    val messageItemImageHeight: Dp = DEFAULT_PADDING, // e.g., "Rectangle", "Circle", "Rounded", "Custom"
    val messageItemClipShape: String = DEFAULT_IMAGE_CLIP_SHAPE,
    val messageItemImageCornerRadius: Dp = DEFAULT_PADDING,
    val messageItemCustomShape: ShapeCornerRadius? = null
)

internal val LocalDesignValues = compositionLocalOf {
    EmbeddedMessagingDesignValues()
}