package com.emarsys.mobileengage.embeddedmessaging.ui.theme

import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.BUTTON_FONT_WEIGHT
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.DEFAULT_BORDER_RADIUS
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.DEFAULT_BORDER_WIDTH
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.DEFAULT_CURSOR
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.DEFAULT_DIVIDER_WIDTH
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.DEFAULT_MARGIN
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.DEFAULT_PADDING
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.DEFAULT_SPACING
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.DIALOG_CARD_WIDTH
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.DIALOG_CONTAINER_PADDING
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.FLOATING_ACTION_BUTTON_SIZE
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.ICON_DEFAULT_SIZE
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.LIST_PANE_WIDTH
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.MAX_HEIGHT
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.MAX_WIDTH
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.MESSAGE_ITEM_IMAGE_SIZE
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.TEXT_PLACEHOLDER_HEIGHT
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.TITLE_FONT_WEIGHT
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.ZERO_BORDER_WIDTH
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.ZERO_MARGIN
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.ZERO_PADDING
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.ZERO_POSITION_VALUE
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.ZERO_SPACING
import org.jetbrains.compose.web.ExperimentalComposeWebApi
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.AnimationTimingFunction
import org.jetbrains.compose.web.css.AnimationTimingFunction.Companion.cubicBezier
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.StyleSheet
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.animation
import org.jetbrains.compose.web.css.background
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.backgroundPosition
import org.jetbrains.compose.web.css.backgroundSize
import org.jetbrains.compose.web.css.border
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.bottom
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.cursor
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.duration
import org.jetbrains.compose.web.css.flex
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.iterationCount
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.left
import org.jetbrains.compose.web.css.margin
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.marginRight
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.maxWidth
import org.jetbrains.compose.web.css.ms
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.position
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.right
import org.jetbrains.compose.web.css.s
import org.jetbrains.compose.web.css.textAlign
import org.jetbrains.compose.web.css.timingFunction
import org.jetbrains.compose.web.css.top
import org.jetbrains.compose.web.css.transitions
import org.jetbrains.compose.web.css.width

object EmbeddedMessagingStyleSheet : StyleSheet() {
    val listPageContainer by style {
        backgroundColor(CssColorVars.colorSurface.value())
        height(MAX_HEIGHT)
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
    }

    val splitViewContainer by style {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Row)
        height(MAX_HEIGHT)
        width(MAX_WIDTH)
    }

    val listPane by style {
        width(LIST_PANE_WIDTH)
        property(
            "border-right",
            "$DEFAULT_BORDER_WIDTH solid ${CssColorVars.colorOutline.value()}"
        )
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
    }

    val detailPane by style {
        flex(1)
        padding(DEFAULT_PADDING)
        property("overflow-y", "auto")
    }

    val compactListView by style {
        backgroundColor(CssColorVars.colorSurface.value())
        height(MAX_HEIGHT)
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
    }

    val filterRowContainer by style {
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        padding(DEFAULT_PADDING)
        gap(ZERO_SPACING)
    }

    val filterButton by style {
        height(FLOATING_ACTION_BUTTON_SIZE)
        padding(DEFAULT_PADDING)
        border(ZERO_BORDER_WIDTH)
        borderRadius(DEFAULT_BORDER_RADIUS)
        cursor(DEFAULT_CURSOR)
        fontSize(CssFontVars.fontSizeBodyMedium)
        fontWeight(BUTTON_FONT_WEIGHT)
        marginRight(DEFAULT_MARGIN)
    }

    val filterButtonUnselected by style {
        backgroundColor(Color.transparent)
        color(CssColorVars.colorOnSurfaceVariant.value())
        border(ZERO_BORDER_WIDTH)
    }

    val filterButtonSelected by style {
        backgroundColor(Color.transparent)
        color(CssColorVars.colorPrimary.value())
        border(ZERO_BORDER_WIDTH)
    }

    @OptIn(ExperimentalComposeWebApi::class)
    val filterButtonSelectedIndicator by style {
        position(Position.Absolute)
        bottom(0.px)
        height(2.px)
        backgroundColor(CssColorVars.colorSurfaceTint.value())
        transitions {
            all {
                duration = 0.3.s
                timingFunction = cubicBezier(0.4, 0.0, 0.2, 1.0)
            }
        }
    }

    val divider by style {
        border(DEFAULT_BORDER_WIDTH, color = CssColorVars.colorOutline.value())
        height(DEFAULT_DIVIDER_WIDTH)
        backgroundColor(CssColorVars.colorOutline.value())
        margin(ZERO_MARGIN)
    }

    val messageListContainer by style {
        flex(1)
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        property("overflow", "scroll")
    }

    val refreshIndicator by style {
        padding(DEFAULT_PADDING)
        textAlign("center")
        color(CssColorVars.colorOnSurface.value())
        fontSize(CssFontVars.fontSizeLabelLarge)
    }

    val refreshButton by style {
        width(MAX_WIDTH)
        padding(DEFAULT_PADDING)
        border(ZERO_BORDER_WIDTH)
        backgroundColor(CssColorVars.colorSurfaceVariant.value())
        color(CssColorVars.colorOnSurfaceVariant.value())
        cursor(DEFAULT_CURSOR)
        fontSize(CssFontVars.fontSizeLabelLarge)
        display(DisplayStyle.Flex)
        justifyContent(JustifyContent.Center)
        alignItems(AlignItems.Center)
    }

    val defaultSvgIconSize by style {
        width(ICON_DEFAULT_SIZE)
        height(ICON_DEFAULT_SIZE)
    }

    val scrollableList by style {
        flex(1)
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
    }

    val emptyStateContainer by style {
        flex(1)
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        justifyContent(JustifyContent.Center)
    }

    val emptyStateContent by style {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        alignItems(AlignItems.Center)
        textAlign("center")
    }

    val emptyStateText by style {
        fontSize(CssFontVars.fontSizeBodyLarge)
        color(CssColorVars.colorOnSurface.value())
        display(DisplayStyle.Block)
    }

    val emptyStateTitle by style {
        marginBottom(DEFAULT_MARGIN)
        fontWeight("bold")
    }

    val dialogOverlay by style {
        position(Position.Fixed)
        top(ZERO_POSITION_VALUE)
        left(ZERO_POSITION_VALUE)
        right(ZERO_POSITION_VALUE)
        bottom(ZERO_POSITION_VALUE)
        backgroundColor(Color("rgba(0, 0, 0, 0.5)"))
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        justifyContent(JustifyContent.Center)
        property("z-index", "1000")
    }

    val dialogCard by style {
        backgroundColor(CssColorVars.colorSurface.value())
        borderRadius(DEFAULT_BORDER_RADIUS)
        property("box-shadow", "0 4px 16px rgba(0,0,0,0.2)")
        maxWidth(DIALOG_CARD_WIDTH)
        width(MAX_WIDTH)
    }

    val dialogContent by style {
        padding(DIALOG_CONTAINER_PADDING)
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        gap(DEFAULT_SPACING)
    }

    val dialogHeaderContainer by style {
        padding(ZERO_PADDING, ZERO_PADDING, ZERO_PADDING, DEFAULT_PADDING)
    }

    val dialogHeaderRow by style {
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
    }

    val dialogTitle by style {
        fontSize(CssFontVars.fontSizeTitleLarge)
        fontWeight(TITLE_FONT_WEIGHT)
        color(CssColorVars.colorOnSurface.value())
        flex(1)
    }

    val dialogCloseButton by style {
        backgroundColor(Color.transparent)
        border(ZERO_BORDER_WIDTH)
        cursor(DEFAULT_CURSOR)
        padding(ZERO_PADDING)
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        justifyContent(JustifyContent.Center)
        color(CssColorVars.colorOnSurface.value())
    }

    val dialogSubtitle by style {
        fontSize(CssFontVars.fontSizeBodyMedium)
        color(CssColorVars.colorOnSurface.value())
        display(DisplayStyle.Block)
        marginTop(DEFAULT_MARGIN)
    }

    val dialogDivider by style {
        border(DEFAULT_BORDER_WIDTH, color = CssColorVars.colorOutline.value())
        height(DEFAULT_DIVIDER_WIDTH)
        backgroundColor(CssColorVars.colorOutline.value())
        margin(ZERO_MARGIN, DEFAULT_MARGIN)
    }

    val categoryChipsContainer by style {
        display(DisplayStyle.Flex)
        property("flex-wrap", "wrap")
        gap(DEFAULT_SPACING)
        padding(DEFAULT_PADDING)
    }

    val categoryChip by style {
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        gap(DEFAULT_SPACING)
        padding(DEFAULT_PADDING)
        border(ZERO_BORDER_WIDTH)
        borderRadius(DEFAULT_BORDER_RADIUS)
        cursor(DEFAULT_CURSOR)
        fontSize(CssFontVars.fontSizeLabelLarge)
        fontWeight(BUTTON_FONT_WEIGHT)
    }

    val categoryChipSelected by style {
        backgroundColor(CssColorVars.colorSecondaryContainer.value())
        color(CssColorVars.colorOnSecondaryContainer.value())
        border(ZERO_BORDER_WIDTH)
    }

    val categoryChipUnselected by style {
        backgroundColor(Color.transparent)
        color(CssColorVars.colorOnPrimaryContainer.value())
        border(ZERO_BORDER_WIDTH)
    }

    val dialogActionsContainer by style {
        display(DisplayStyle.Flex)
        padding(DEFAULT_PADDING)
        gap(DEFAULT_SPACING)
    }

    val deleteDialogActionsContainer by style {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Row)
        alignItems(AlignItems.Center)
        justifyContent(JustifyContent.End)
        padding(DEFAULT_PADDING)
        gap(DEFAULT_SPACING)
    }

    val dialogResetButton by style {
        padding(DEFAULT_PADDING)
        border(ZERO_BORDER_WIDTH)
        borderRadius(DEFAULT_BORDER_RADIUS)
        backgroundColor(Color.transparent)
        color(CssColorVars.colorPrimary.value())
        cursor(DEFAULT_CURSOR)
        fontSize(CssFontVars.fontSizeLabelLarge)
        fontWeight(BUTTON_FONT_WEIGHT)
    }

    val dialogApplyButton by style {
        padding(DEFAULT_BORDER_RADIUS)
        border(ZERO_BORDER_WIDTH)
        borderRadius(DEFAULT_BORDER_RADIUS)
        backgroundColor(CssColorVars.colorPrimary.value())
        color(CssColorVars.colorOnPrimary.value())
        cursor(DEFAULT_CURSOR)
        fontSize(CssFontVars.fontSizeLabelLarge)
        fontWeight(BUTTON_FONT_WEIGHT)
        property("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
    }

    val spacer by style {
        flex(1)
    }

    val categorySelectorButton by style {
        height(FLOATING_ACTION_BUTTON_SIZE)
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        gap(DEFAULT_SPACING)
        padding(DEFAULT_PADDING)
        border(ZERO_BORDER_WIDTH)
        borderRadius(DEFAULT_BORDER_RADIUS)
        cursor(DEFAULT_CURSOR)
        fontSize(CssFontVars.fontSizeLabelLarge)
        fontWeight(BUTTON_FONT_WEIGHT)
        property("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
    }

    val categorySelectorButtonActive by style {
        backgroundColor(CssColorVars.colorPrimary.value())
        color(CssColorVars.colorOnPrimary.value())
        border(ZERO_BORDER_WIDTH)
    }

    val categorySelectorButtonInactive by style {
        backgroundColor(CssColorVars.colorSurfaceVariant.value())
        color(CssColorVars.colorOnSurfaceVariant.value())
        border(ZERO_BORDER_WIDTH)
    }

    val deleteMessageIcon by style {
        padding(DEFAULT_PADDING)
        borderRadius(DEFAULT_BORDER_RADIUS)
        marginRight(DEFAULT_MARGIN)
        background("radial-gradient(circle, transparent, var(--emarsys-color-surface-dim))")
        property("opacity", 0)
        property("pointer-events", "none")
        property("transform", "scale(0.8)")
        property("transition", "opacity 0.2s ease-out, transform 0.2s ease-out")
    }

    val messageItem by style {
        position(Position.Relative)
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Row)
        alignItems(AlignItems.Center)
        padding(DEFAULT_PADDING)
        cursor(DEFAULT_CURSOR)

        self + hover style {
            child(
                className("EmbeddedMessagingStyleSheet-messageItemMisc"),
                className("EmbeddedMessagingStyleSheet-deleteMessageIcon")
            ) style {
                property("opacity", 1)
                property("pointer-events", "auto")
                property("transform", "scale(1)")
            }
        }
    }

    val messageItemHover by style {
        self + hover style {
            backgroundColor(CssColorVars.colorSurfaceVariant.value())
        }
    }

    val messageItemSelected by style {
        backgroundColor(CssColorVars.colorSurfaceVariant.value())
    }

    val messageItemImage by style {
        width(MESSAGE_ITEM_IMAGE_SIZE)
        height(MESSAGE_ITEM_IMAGE_SIZE)
        property("object-fit", "cover")
    }

    val messageItemImageSpacer by style {
        padding(DEFAULT_PADDING)
    }

    val messageItemContent by style {
        flex(1)
        padding(DEFAULT_PADDING)
        property("overflow", "hidden")
    }

    val messageItemMisc by style {
        position(Position.Absolute)
        right(0.px)
        top(0.px)
        bottom(0.px)
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        justifyContent(JustifyContent.Center)
        alignItems(AlignItems.Center)
        padding(DEFAULT_PADDING)
    }

    val messageItemContentNoPadding by style {
        flex(1)
        padding(ZERO_PADDING)
    }

    val messageItemTitle by style {
        display(DisplayStyle.Block)
        marginBottom(DEFAULT_MARGIN)
        fontSize(CssFontVars.fontSizeBodyLarge)
        color(CssColorVars.colorOnSurface.value())
    }

    val unopened by style {
        fontWeight("bold")
    }

    val messageItemLead by style {
        display(DisplayStyle.Block)
        marginBottom(DEFAULT_MARGIN)
        fontSize(CssFontVars.fontSizeBodyMedium)
        color(CssColorVars.colorOnSurface.value())
        property("overflow", "hidden")
        property("text-overflow", "ellipsis")
        property("white-space", "nowrap")
    }

    val messageItemTimestamp by style {
        fontSize(CssFontVars.fontSizeBodyMedium)
        color(CssColorVars.colorOnSurface.value())
        display(DisplayStyle.Block)
    }

    val messageItemTextPlaceholder by style {
        height(TEXT_PLACEHOLDER_HEIGHT)
    }

    val loadingSpinner by style {
        width(MESSAGE_ITEM_IMAGE_SIZE)
        height(MESSAGE_ITEM_IMAGE_SIZE)
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        justifyContent(JustifyContent.Center)
        color(CssColorVars.colorOnSurface.value())
    }

    val detailViewContainer by style {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        gap(DEFAULT_SPACING)
    }

    val detailContent by style {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        gap(DEFAULT_PADDING)
    }

    val detailImage by style {
        maxWidth(MAX_WIDTH)
        borderRadius(DEFAULT_BORDER_RADIUS)
        property("object-fit", "contain")
    }

    private val shimmer by keyframes {
        0.percent {
            backgroundPosition("100% 100%")
        }
        70.percent {
            backgroundPosition("0% 0%")
        }
        100.percent {
            backgroundPosition("0% 0%")
        }
    }

    val shimmerEffect by style {
        animation(shimmer) {
            duration(1200.ms)
            timingFunction(AnimationTimingFunction.EaseInOut)
            iterationCount(null)  // infinite
        }
        background(
            """linear-gradient(
                -45deg, #0000001A 25%, 
                ${CssColorVars.colorSurfaceTint.value()} 50%,
                #0000001A 75%)"""
                .trimIndent()
        )
        backgroundSize("400% 400%")
    }
}
