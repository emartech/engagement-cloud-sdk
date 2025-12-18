package com.emarsys.mobileengage.embeddedmessaging.ui.theme

import org.jetbrains.compose.web.css.CSSColorValue
import org.jetbrains.compose.web.css.CSSNumeric
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.StyleScope
import org.jetbrains.compose.web.css.px

data class CssColorVar(val name: String) {
    fun value(): CSSColorValue = Color("var($name)")
}

fun StyleScope.setVar(variable: CssColorVar, value: String) {
    property(variable.name, value)
}

internal object CssColorVars {
    val colorPrimary = CssColorVar("--emarsys-color-primary")
    val colorOnPrimary = CssColorVar("--emarsys-color-on-primary")
    val colorPrimaryContainer = CssColorVar("--emarsys-color-primary-container")
    val colorOnPrimaryContainer = CssColorVar("--emarsys-color-on-primary-container")

    val colorSecondary = CssColorVar("--emarsys-color-secondary")
    val colorOnSecondary = CssColorVar("--emarsys-color-on-secondary")
    val colorSecondaryContainer = CssColorVar("--emarsys-color-secondary-container")
    val colorOnSecondaryContainer = CssColorVar("--emarsys-color-on-secondary-container")

    val colorTertiary = CssColorVar("--emarsys-color-tertiary")
    val colorOnTertiary = CssColorVar("--emarsys-color-on-tertiary")
    val colorTertiaryContainer = CssColorVar("--emarsys-color-tertiary-container")
    val colorOnTertiaryContainer = CssColorVar("--emarsys-color-on-tertiary-container")

    val colorError = CssColorVar("--emarsys-color-error")
    val colorOnError = CssColorVar("--emarsys-color-on-error")
    val colorErrorContainer = CssColorVar("--emarsys-color-error-container")
    val colorOnErrorContainer = CssColorVar("--emarsys-color-on-error-container")

    val colorBackground = CssColorVar("--emarsys-color-background")
    val colorOnBackground = CssColorVar("--emarsys-color-on-background")
    val colorSurface = CssColorVar("--emarsys-color-surface")
    val colorOnSurface = CssColorVar("--emarsys-color-on-surface")
    val colorSurfaceVariant = CssColorVar("--emarsys-color-surface-variant")
    val colorOnSurfaceVariant = CssColorVar("--emarsys-color-on-surface-variant")

    val colorSurfaceContainer = CssColorVar("--emarsys-color-surface-container")
    val colorSurfaceContainerHigh = CssColorVar("--emarsys-color-surface-container-high")
    val colorSurfaceContainerHighest = CssColorVar("--emarsys-color-surface-container-highest")
    val colorSurfaceContainerLow = CssColorVar("--emarsys-color-surface-container-low")
    val colorSurfaceContainerLowest = CssColorVar("--emarsys-color-surface-container-lowest")
    val colorSurfaceDim = CssColorVar("--emarsys-color-surface-dim")
    val colorSurfaceBright = CssColorVar("--emarsys-color-surface-bright")
    val colorSurfaceTint = CssColorVar("--emarsys-color-surface-tint")

    val colorOutline = CssColorVar("--emarsys-color-outline")
    val colorOutlineVariant = CssColorVar("--emarsys-color-outline-variant")

    val colorInverseSurface = CssColorVar("--emarsys-color-inverse-surface")
    val colorInverseOnSurface = CssColorVar("--emarsys-color-inverse-on-surface")
    val colorInversePrimary = CssColorVar("--emarsys-color-inverse-primary")

    val colorScrim = CssColorVar("--emarsys-color-scrim")

    val colorPrimaryFixed = CssColorVar("--emarsys-color-colorPrimaryFixed")
    val colorPrimaryFixedDim = CssColorVar("--emarsys-color-colorPrimaryFixedDim")
    val colorOnPrimaryFixed = CssColorVar("--emarsys-color-colorOnPrimaryFixed")
    val colorOnPrimaryFixedVariant = CssColorVar("--emarsys-color-colorOnPrimaryFixedVariant")
    val colorSecondaryFixed = CssColorVar("--emarsys-color-colorSecondaryFixed")
    val colorSecondaryFixedDim = CssColorVar("--emarsys-color-colorSecondaryFixedDim")
    val colorOnSecondaryFixed = CssColorVar("--emarsys-color-colorOnSecondaryFixed")
    val colorOnSecondaryFixedVariant = CssColorVar("--emarsys-color-colorOnSecondaryFixedVariant")
    val colorTertiaryFixed = CssColorVar("--emarsys-color-colorTertiaryFixed")
    val colorTertiaryFixedDim = CssColorVar("--emarsys-color-colorTertiaryFixedDim")
    val colorOnTertiaryFixed = CssColorVar("--emarsys-color-colorOnTertiaryFixed")
    val colorOnTertiaryFixedVariant = CssColorVar("--emarsys-color-colorOnTertiaryFixedVariant")
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

internal object DefaultFontVars {
    val fontSizeDisplayLarge: CSSNumeric = 56.px
    val fontSizeDisplayMedium: CSSNumeric = 44.px
    val fontSizeDisplaySmall: CSSNumeric = 36.px
    val fontSizeHeadlineLarge: CSSNumeric = 32.px
    val fontSizeHeadlineMedium: CSSNumeric = 28.px
    val fontSizeHeadlineSmall: CSSNumeric = 24.px
    val fontSizeTitleLarge: CSSNumeric = 20.px
    val fontSizeTitleMedium: CSSNumeric = 16.px
    val fontSizeTitleSmall: CSSNumeric = 14.px
    val fontSizeBodyLarge: CSSNumeric = 16.px
    val fontSizeBodyMedium: CSSNumeric = 14.px
    val fontSizeBodySmall: CSSNumeric = 12.px
    val fontSizeLabelLarge: CSSNumeric = 14.px
    val fontSizeLabelMedium: CSSNumeric = 12.px
    val fontSizeLabelSmall: CSSNumeric = 10.px
}
