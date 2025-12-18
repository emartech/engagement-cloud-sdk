package com.emarsys.mobileengage.embeddedmessaging.ui.theme

import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.DEFAULT_PADDING
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.DEFAULT_SPACING
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.DIALOG_CONTAINER_PADDING
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.FLOATING_ACTION_BUTTON_SIZE
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.MESSAGE_ITEM_IMAGE_SIZE
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.ZERO_PADDING
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.ZERO_SPACING
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.AlignSelf
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.StyleSheet
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.alignSelf
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.border
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.bottom
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.cursor
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flex
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.left
import org.jetbrains.compose.web.css.margin
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.marginRight
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.maxWidth
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.position
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.right
import org.jetbrains.compose.web.css.textAlign
import org.jetbrains.compose.web.css.top
import org.jetbrains.compose.web.css.vh
import org.jetbrains.compose.web.css.width

object EmbeddedMessagingStyleSheet : StyleSheet() {
    val listPageContainer by style {
        backgroundColor(CssVars.colorSurface.value())
        height(100.vh)
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
    }

    val splitViewContainer by style {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Row)
        height(100.percent)
        width(100.percent)
    }

    val listPane by style {
        width(400.px)
        property("border-right", "1px solid ${CssVars.colorOutline.variableName()}")
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
    }

    val detailPane by style {
        flex(1)
        padding(DEFAULT_PADDING)
        property("overflow-y", "auto")
    }

    val filterRowContainer by style {
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        padding(DEFAULT_PADDING)
        gap(ZERO_SPACING)
    }

    val filterButton by style {
        height(FLOATING_ACTION_BUTTON_SIZE)
        padding(8.px, 12.px)
        borderRadius(16.px)
        cursor("pointer")
        fontSize(14.px)
        fontWeight(400)
        marginRight(8.px)
    }

    val filterButtonUnselected by style {
        backgroundColor(Color.transparent)
        color(CssVars.colorOnSurfaceVariant.value())
        border(1.px, LineStyle.Solid, CssVars.colorOutline.value())
    }

    val filterButtonSelected by style {
        backgroundColor(CssVars.colorSecondaryContainer.value())
        color(CssVars.colorOnSecondaryContainer.value())
        border(0.px)
    }

    val divider by style {
        border(1.px)
        height(1.px)
        backgroundColor(CssVars.colorOutline.value())
        margin(0.px)
    }

    val messageListContainer by style {
        flex(1)
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        property("overflow", "hidden")
    }

    val refreshIndicator by style {
        padding(8.px)
        textAlign("center")
        color(CssVars.colorOnSurface.value())
        fontSize(14.px)
    }

    val refreshButton by style {
        width(100.percent)
        padding(8.px)
        border(0.px)
        backgroundColor(CssVars.colorSurfaceVariant.value())
        color(CssVars.colorOnSurfaceVariant.value())
        cursor("pointer")
        fontSize(14.px)
        display(DisplayStyle.Flex)
        justifyContent(JustifyContent.Center)
        alignItems(AlignItems.Center)
    }

    val refreshIcon by style {
        width(24.px)
        height(24.px)
    }

    val scrollableList by style {
        flex(1)
        property("overflow-y", "auto")
    }

    val emptyStateContainer by style {
        flex(1)
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        justifyContent(JustifyContent.Center)
        property("overflow-y", "auto")
    }

    val emptyStateContent by style {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        alignItems(AlignItems.Center)
        textAlign("center")
    }

    val emptyStateText by style {
        fontSize(16.px)
        color(CssVars.colorOnSurface.value())
        display(DisplayStyle.Block)
    }

    val emptyStateTitle by style {
        marginBottom(8.px)
    }

    val dialogOverlay by style {
        position(Position.Fixed)
        top(0.px)
        left(0.px)
        right(0.px)
        bottom(0.px)
        backgroundColor(Color("rgba(0, 0, 0, 0.5)"))
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        justifyContent(JustifyContent.Center)
        property("z-index", "1000")
    }

    val dialogCard by style {
        backgroundColor(CssVars.colorSurface.value())
        borderRadius(8.px)
        property("box-shadow", "0 4px 16px rgba(0,0,0,0.2)")
        maxWidth(500.px)
        width(90.percent)
    }

    val dialogContent by style {
        padding(DIALOG_CONTAINER_PADDING)
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        gap(DEFAULT_SPACING)
    }

    val dialogHeaderContainer by style {
        padding(0.px, 0.px, 0.px, DEFAULT_PADDING)
    }

    val dialogHeaderRow by style {
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
    }

    val dialogTitle by style {
        fontSize(22.px)
        fontWeight(500)
        color(CssVars.colorOnSurface.value())
        flex(1)
    }

    val dialogCloseButton by style {
        border(0.px)
        backgroundColor(Color.transparent)
        cursor("pointer")
        width(24.px)
        height(24.px)
        padding(0.px) // Reset padding for icon-only button
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        justifyContent(JustifyContent.Center)
        color(CssVars.colorOnSurface.value())
    }

    val dialogSubtitle by style {
        fontSize(14.px)
        color(CssVars.colorOnSurface.value())
        display(DisplayStyle.Block)
        marginTop(4.px)
    }

    val dialogDivider by style {
        border(0.px)
        height(1.px)
        backgroundColor(CssVars.colorOutline.value())
        margin(0.px, DEFAULT_PADDING)
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
        padding(8.px, 12.px)
        borderRadius(16.px)
        cursor("pointer")
        fontSize(14.px)
        fontWeight(400)
    }

    val categoryChipSelected by style {
        backgroundColor(CssVars.colorSecondaryContainer.value())
        color(CssVars.colorOnSecondaryContainer.value())
        border(0.px)
    }

    val categoryChipUnselected by style {
        backgroundColor(Color.transparent)
        color(CssVars.colorOnPrimaryContainer.value())
        property("border", "1px solid ${CssVars.colorOutline.variableName()}")
    }

    val categoryChipCheckmark by style {
        width(18.px)
        height(18.px)
    }

    val dialogActionsContainer by style {
        display(DisplayStyle.Flex)
        padding(DEFAULT_PADDING)
        gap(8.px)
    }

    val dialogResetButton by style {
        padding(10.px, 16.px)
        borderRadius(4.px)
        border(0.px)
        backgroundColor(Color.transparent)
        color(CssVars.colorPrimary.value())
        cursor("pointer")
        fontSize(14.px)
        fontWeight(500)
    }

    val dialogApplyButton by style {
        padding(10.px, 16.px)
        borderRadius(4.px)
        border(0.px)
        backgroundColor(CssVars.colorPrimary.value())
        color(CssVars.colorOnPrimary.value())
        cursor("pointer")
        fontSize(14.px)
        fontWeight(500)
        property("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
    }

    val spacer by style {
        flex(1)
    }

    val categorySelectorButton by style {
        height(FLOATING_ACTION_BUTTON_SIZE)
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        gap(8.px)
        padding(8.px, 16.px)
        borderRadius(4.px)
        border(0.px)
        cursor("pointer")
        fontSize(14.px)
        fontWeight(500)
        property("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
    }

    val categorySelectorButtonActive by style {
        backgroundColor(CssVars.colorPrimary.value())
        color(CssVars.colorOnPrimary.value())
    }

    val categorySelectorButtonInactive by style {
        backgroundColor(CssVars.colorSurfaceVariant.value())
        color(CssVars.colorOnSurfaceVariant.value())
    }

    val categorySelectorIcon by style {
        width(18.px)
        height(18.px)
        property("fill", "currentColor") // Ensure SVG takes text color
    }

    val messageItem by style {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Row)
        alignItems(AlignItems.Center)
        padding(DEFAULT_PADDING)
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
    }

    val messageItemContentNoPadding by style {
        flex(1)
        padding(ZERO_PADDING)
    }

    val messageItemTitle by style {
        fontSize(16.px)
        color(CssVars.colorOnSurface.value())
        display(DisplayStyle.Block)
    }

    val messageItemLead by style {
        fontSize(16.px)
        color(CssVars.colorOnSurface.value())
        property("overflow", "hidden")
        property("text-overflow", "ellipsis")
        property("white-space", "nowrap")
        display(DisplayStyle.Block)
    }

    val messageItemTimestamp by style {
        fontSize(16.px)
        color(CssVars.colorOnSurface.value())
    }

    val loadingSpinner by style {
        width(MESSAGE_ITEM_IMAGE_SIZE)
        height(MESSAGE_ITEM_IMAGE_SIZE)
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        justifyContent(JustifyContent.Center)
        color(CssVars.colorOnSurface.value())
    }

    val detailViewContainer by style {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        gap(DEFAULT_SPACING)
    }

    val detailBackButton by style {
        padding(8.px, 16.px)
        borderRadius(4.px)
        border(0.px)
        backgroundColor(CssVars.colorSurfaceVariant.value())
        color(CssVars.colorOnSurface.value())
        cursor("pointer")
        alignSelf(AlignSelf.FlexStart)
        marginBottom(DEFAULT_PADDING)
    }

    val detailContent by style {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        gap(DEFAULT_PADDING)
    }

    val detailImage by style {
        maxWidth(100.percent)
        borderRadius(8.px)
        property("object-fit", "contain")
    }
}
