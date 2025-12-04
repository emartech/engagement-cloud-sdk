package com.emarsys.mobileengage.embeddedmessaging.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.DEFAULT_ELEVATION
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.DEFAULT_PADDING
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.ZERO_SPACING
import com.emarsys.networking.clients.embedded.messaging.model.ShapeCornerRadius
import org.jetbrains.compose.web.css.CSSSizeValue
import org.jetbrains.compose.web.css.CSSUnit

@Immutable
internal data class EmbeddedMessagingDesignValues(
    val messageItemMargin: CSSSizeValue<CSSUnit.px> = DEFAULT_PADDING,
    val messageItemElevation: CSSSizeValue<CSSUnit.px> = DEFAULT_ELEVATION,

    val buttonElevation: CSSSizeValue<CSSUnit.px> = DEFAULT_ELEVATION,

    val listContentPadding: CSSSizeValue<CSSUnit.px> = DEFAULT_PADDING,
    val listItemSpacing: CSSSizeValue<CSSUnit.px> = ZERO_SPACING,

    val compactOverlayWidth: CSSSizeValue<CSSUnit.px> = DEFAULT_PADDING,
    val compactOverlayMaxHeight: CSSSizeValue<CSSUnit.px> = DEFAULT_PADDING,
    val compactOverlayCornerRadius: CSSSizeValue<CSSUnit.px> = DEFAULT_PADDING,
    val compactOverlayElevation: CSSSizeValue<CSSUnit.px> = DEFAULT_ELEVATION,

    val emptyStateImageUrl: String? = null,
    val messageItemImageHeight: CSSSizeValue<CSSUnit.px> = DEFAULT_PADDING,
    val messageItemClipShape: String = "Rectangle", // e.g., "Rectangle", "Circle", "Rounded", "Custom"
    val messageItemImageCornerRadius: CSSSizeValue<CSSUnit.px> = DEFAULT_PADDING,
    val messageItemCustomShape: ShapeCornerRadius? = null
)

internal val LocalDesignValues = compositionLocalOf {
    EmbeddedMessagingDesignValues()
}
