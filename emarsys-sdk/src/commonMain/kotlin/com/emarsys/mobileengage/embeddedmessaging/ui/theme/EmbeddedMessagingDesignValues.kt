package com.emarsys.mobileengage.embeddedmessaging.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.DEFAULT_ELEVATION
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.DEFAULT_PADDING
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.ZERO_SPACING
import com.emarsys.networking.clients.embedded.messaging.model.ShapeCornerRadius

@Immutable
data class EmbeddedMessagingDesignValues(
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
    val messageItemImageHeight: Dp = DEFAULT_PADDING,
    val messageItemClipShape: String = "Rectangle", // e.g., "Rectangle", "Circle", "Rounded", "Custom"
    val messageItemImageCornerRadius: Dp = DEFAULT_PADDING,
    val messageItemCustomShape: ShapeCornerRadius? = null
)

val LocalDesignValues = compositionLocalOf {
    EmbeddedMessagingDesignValues()
}


