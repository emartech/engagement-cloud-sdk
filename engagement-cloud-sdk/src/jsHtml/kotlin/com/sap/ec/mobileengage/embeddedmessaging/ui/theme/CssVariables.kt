package com.sap.ec.mobileengage.embeddedmessaging.ui.theme

import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.EXTRA_LARGE_CORNER_RADIUS
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.EXTRA_SMALL_CORNER_RADIUS
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.LARGE_CORNER_RADIUS
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.MEDIUM_CORNER_RADIUS
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.SMALL_CORNER_RADIUS
import org.jetbrains.compose.web.css.CSSColorValue
import org.jetbrains.compose.web.css.CSSNumeric
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.StyleScope
import org.jetbrains.compose.web.css.px

internal data class CssColorVar(val name: String) {
    fun value(): CSSColorValue = Color("var($name)")
}

internal fun StyleScope.setVar(variable: CssColorVar, value: String) {
    property(variable.name, value)
}

internal object CssColorVars {
    val colorPrimary =
        CssColorVar("--sap-color-primary")
    val colorOnPrimary =
        CssColorVar("--sap-color-on-primary")
    val colorPrimaryContainer =
        CssColorVar("--sap-color-primary-container")
    val colorOnPrimaryContainer =
        CssColorVar("--sap-color-on-primary-container")

    val colorSecondary =
        CssColorVar("--sap-color-secondary")
    val colorOnSecondary =
        CssColorVar("--sap-color-on-secondary")
    val colorSecondaryContainer =
        CssColorVar("--sap-color-secondary-container")
    val colorOnSecondaryContainer =
        CssColorVar("--sap-color-on-secondary-container")

    val colorTertiary =
        CssColorVar("--sap-color-tertiary")
    val colorOnTertiary =
        CssColorVar("--sap-color-on-tertiary")
    val colorTertiaryContainer =
        CssColorVar("--sap-color-tertiary-container")
    val colorOnTertiaryContainer =
        CssColorVar("--sap-color-on-tertiary-container")

    val colorError =
        CssColorVar("--sap-color-error")
    val colorOnError =
        CssColorVar("--sap-color-on-error")
    val colorErrorContainer =
        CssColorVar("--sap-color-error-container")
    val colorOnErrorContainer =
        CssColorVar("--sap-color-on-error-container")

    val colorBackground =
        CssColorVar("--sap-color-background")
    val colorOnBackground =
        CssColorVar("--sap-color-on-background")
    val colorSurface =
        CssColorVar("--sap-color-surface")
    val colorOnSurface =
        CssColorVar("--sap-color-on-surface")
    val colorSurfaceVariant =
        CssColorVar("--sap-color-surface-variant")
    val colorOnSurfaceVariant =
        CssColorVar("--sap-color-on-surface-variant")

    val colorSurfaceContainer =
        CssColorVar("--sap-color-surface-container")
    val colorSurfaceContainerHigh =
        CssColorVar("--sap-color-surface-container-high")
    val colorSurfaceContainerHighest =
        CssColorVar("--sap-color-surface-container-highest")
    val colorSurfaceContainerLow =
        CssColorVar("--sap-color-surface-container-low")
    val colorSurfaceContainerLowest =
        CssColorVar("--sap-color-surface-container-lowest")
    val colorSurfaceDim =
        CssColorVar("--sap-color-surface-dim")
    val colorSurfaceBright =
        CssColorVar("--sap-color-surface-bright")
    val colorSurfaceTint =
        CssColorVar("--sap-color-surface-tint")

    val colorOutline =
        CssColorVar("--sap-color-outline")
    val colorOutlineVariant =
        CssColorVar("--sap-color-outline-variant")

    val colorInverseSurface =
        CssColorVar("--sap-color-inverse-surface")
    val colorInverseOnSurface =
        CssColorVar("--sap-color-inverse-on-surface")
    val colorInversePrimary =
        CssColorVar("--sap-color-inverse-primary")

    val colorScrim =
        CssColorVar("--sap-color-scrim")

    val colorPrimaryFixed =
        CssColorVar("--sap-color-colorPrimaryFixed")
    val colorPrimaryFixedDim =
        CssColorVar("--sap-color-colorPrimaryFixedDim")
    val colorOnPrimaryFixed =
        CssColorVar("--sap-color-colorOnPrimaryFixed")
    val colorOnPrimaryFixedVariant =
        CssColorVar("--sap-color-colorOnPrimaryFixedVariant")
    val colorSecondaryFixed =
        CssColorVar("--sap-color-colorSecondaryFixed")
    val colorSecondaryFixedDim =
        CssColorVar("--sap-color-colorSecondaryFixedDim")
    val colorOnSecondaryFixed =
        CssColorVar("--sap-color-colorOnSecondaryFixed")
    val colorOnSecondaryFixedVariant =
        CssColorVar("--sap-color-colorOnSecondaryFixedVariant")
    val colorTertiaryFixed =
        CssColorVar("--sap-color-colorTertiaryFixed")
    val colorTertiaryFixedDim =
        CssColorVar("--sap-color-colorTertiaryFixedDim")
    val colorOnTertiaryFixed =
        CssColorVar("--sap-color-colorOnTertiaryFixed")
    val colorOnTertiaryFixedVariant =
        CssColorVar("--sap-color-colorOnTertiaryFixedVariant")
}

internal object CssFontVars {
    var fontSizeDisplayLarge: CSSNumeric = DefaultFontVars.fontSizeDisplayLarge
    var fontSizeDisplayMedium: CSSNumeric = DefaultFontVars.fontSizeDisplayMedium
    var fontSizeDisplaySmall: CSSNumeric = DefaultFontVars.fontSizeDisplaySmall
    var fontSizeHeadlineLarge: CSSNumeric = DefaultFontVars.fontSizeHeadlineLarge
    var fontSizeHeadlineMedium: CSSNumeric = DefaultFontVars.fontSizeHeadlineMedium
    var fontSizeHeadlineSmall: CSSNumeric = DefaultFontVars.fontSizeHeadlineSmall
    var fontSizeTitleLarge: CSSNumeric = DefaultFontVars.fontSizeTitleLarge
    var fontSizeTitleMedium: CSSNumeric = DefaultFontVars.fontSizeTitleMedium
    var fontSizeTitleSmall: CSSNumeric = DefaultFontVars.fontSizeTitleSmall
    var fontSizeBodyLarge: CSSNumeric = DefaultFontVars.fontSizeBodyLarge
    var fontSizeBodyMedium: CSSNumeric = DefaultFontVars.fontSizeBodyMedium
    var fontSizeBodySmall: CSSNumeric = DefaultFontVars.fontSizeBodySmall
    var fontSizeLabelLarge: CSSNumeric = DefaultFontVars.fontSizeLabelLarge
    var fontSizeLabelMedium: CSSNumeric = DefaultFontVars.fontSizeLabelMedium
    var fontSizeLabelSmall: CSSNumeric = DefaultFontVars.fontSizeLabelSmall
}

internal object CssShapeVars {
    var extraSmallTopStart: CSSNumeric = DefaultShapeVars.extraSmallTopStart
    var extraSmallTopEnd: CSSNumeric = DefaultShapeVars.extraSmallTopEnd
    var extraSmallBottomStart: CSSNumeric = DefaultShapeVars.extraSmallBottomStart
    var extraSmallBottomEnd: CSSNumeric = DefaultShapeVars.extraSmallBottomEnd

    var smallTopStart: CSSNumeric = DefaultShapeVars.smallTopStart
    var smallTopEnd: CSSNumeric = DefaultShapeVars.smallTopEnd
    var smallBottomStart: CSSNumeric = DefaultShapeVars.smallBottomStart
    var smallBottomEnd: CSSNumeric = DefaultShapeVars.smallBottomEnd

    var mediumTopStart: CSSNumeric = DefaultShapeVars.mediumTopStart
    var mediumTopEnd: CSSNumeric = DefaultShapeVars.mediumTopEnd
    var mediumBottomStart: CSSNumeric = DefaultShapeVars.mediumBottomStart
    var mediumBottomEnd: CSSNumeric = DefaultShapeVars.mediumBottomEnd

    var largeTopStart: CSSNumeric = DefaultShapeVars.largeTopStart
    var largeTopEnd: CSSNumeric = DefaultShapeVars.largeTopEnd
    var largeBottomStart: CSSNumeric = DefaultShapeVars.largeBottomStart
    var largeBottomEnd: CSSNumeric = DefaultShapeVars.largeBottomEnd

    var extraLargeTopStart: CSSNumeric = DefaultShapeVars.extraLargeTopStart
    var extraLargeTopEnd: CSSNumeric = DefaultShapeVars.extraLargeTopEnd
    var extraLargeBottomStart: CSSNumeric = DefaultShapeVars.extraLargeBottomStart
    var extraLargeBottomEnd: CSSNumeric = DefaultShapeVars.extraLargeBottomEnd
}

internal object DefaultShapeVars {
    val extraSmallTopStart: CSSNumeric = EXTRA_SMALL_CORNER_RADIUS
    val extraSmallTopEnd: CSSNumeric = EXTRA_SMALL_CORNER_RADIUS
    val extraSmallBottomStart: CSSNumeric = EXTRA_SMALL_CORNER_RADIUS
    val extraSmallBottomEnd: CSSNumeric = EXTRA_SMALL_CORNER_RADIUS

    val smallTopStart: CSSNumeric = SMALL_CORNER_RADIUS
    val smallTopEnd: CSSNumeric = SMALL_CORNER_RADIUS
    val smallBottomStart: CSSNumeric = SMALL_CORNER_RADIUS
    val smallBottomEnd: CSSNumeric = SMALL_CORNER_RADIUS

    val mediumTopStart: CSSNumeric = MEDIUM_CORNER_RADIUS
    val mediumTopEnd: CSSNumeric = MEDIUM_CORNER_RADIUS
    val mediumBottomStart: CSSNumeric = MEDIUM_CORNER_RADIUS
    val mediumBottomEnd: CSSNumeric = MEDIUM_CORNER_RADIUS

    val largeTopStart: CSSNumeric = LARGE_CORNER_RADIUS
    val largeTopEnd: CSSNumeric = LARGE_CORNER_RADIUS
    val largeBottomStart: CSSNumeric = LARGE_CORNER_RADIUS
    val largeBottomEnd: CSSNumeric = LARGE_CORNER_RADIUS

    val extraLargeTopStart: CSSNumeric = EXTRA_LARGE_CORNER_RADIUS
    val extraLargeTopEnd: CSSNumeric = EXTRA_LARGE_CORNER_RADIUS
    val extraLargeBottomStart: CSSNumeric = EXTRA_LARGE_CORNER_RADIUS
    val extraLargeBottomEnd: CSSNumeric = EXTRA_LARGE_CORNER_RADIUS
}

internal object DefaultFontVars {
    val fontSizeDisplayLarge: CSSNumeric = 56.px
    val fontSizeDisplayMedium: CSSNumeric = 44.px
    val fontSizeDisplaySmall: CSSNumeric = 36.px
    val fontSizeHeadlineLarge: CSSNumeric = 32.px
    val fontSizeHeadlineMedium: CSSNumeric = 28.px
    val fontSizeHeadlineSmall: CSSNumeric = 24.px
    val fontSizeTitleLarge: CSSNumeric = 24.px
    val fontSizeTitleMedium: CSSNumeric = 17.px
    val fontSizeTitleSmall: CSSNumeric = 14.px
    val fontSizeBodyLarge: CSSNumeric = 17.px
    val fontSizeBodyMedium: CSSNumeric = 15.px
    val fontSizeBodySmall: CSSNumeric = 13.px
    val fontSizeLabelLarge: CSSNumeric = 17.px
    val fontSizeLabelMedium: CSSNumeric = 15.px
    val fontSizeLabelSmall: CSSNumeric = 10.px
}
