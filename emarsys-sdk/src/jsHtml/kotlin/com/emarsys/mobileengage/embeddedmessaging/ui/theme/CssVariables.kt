package com.emarsys.mobileengage.embeddedmessaging.ui.theme

import org.jetbrains.compose.web.css.CSSColorValue
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.StyleScope

data class CssVar(val name: String) {
    fun value(): CSSColorValue = Color("var($name)")
    
    fun variableName(): String = "var($name)"
}

fun StyleScope.setVar(variable: CssVar, value: String) {
    property(variable.name, value)
}

object CssVars {
    val colorPrimary = CssVar("--emarsys-color-primary")
    val colorOnPrimary = CssVar("--emarsys-color-on-primary")
    val colorPrimaryContainer = CssVar("--emarsys-color-primary-container")
    val colorOnPrimaryContainer = CssVar("--emarsys-color-on-primary-container")
    
    val colorSecondary = CssVar("--emarsys-color-secondary")
    val colorOnSecondary = CssVar("--emarsys-color-on-secondary")
    val colorSecondaryContainer = CssVar("--emarsys-color-secondary-container")
    val colorOnSecondaryContainer = CssVar("--emarsys-color-on-secondary-container")
    
    val colorTertiary = CssVar("--emarsys-color-tertiary")
    val colorOnTertiary = CssVar("--emarsys-color-on-tertiary")
    val colorTertiaryContainer = CssVar("--emarsys-color-tertiary-container")
    val colorOnTertiaryContainer = CssVar("--emarsys-color-on-tertiary-container")
    
    val colorError = CssVar("--emarsys-color-error")
    val colorOnError = CssVar("--emarsys-color-on-error")
    val colorErrorContainer = CssVar("--emarsys-color-error-container")
    val colorOnErrorContainer = CssVar("--emarsys-color-on-error-container")
    
    val colorBackground = CssVar("--emarsys-color-background")
    val colorOnBackground = CssVar("--emarsys-color-on-background")
    val colorSurface = CssVar("--emarsys-color-surface")
    val colorOnSurface = CssVar("--emarsys-color-on-surface")
    val colorSurfaceVariant = CssVar("--emarsys-color-surface-variant")
    val colorOnSurfaceVariant = CssVar("--emarsys-color-on-surface-variant")
    
    val colorSurfaceContainer = CssVar("--emarsys-color-surface-container")
    val colorSurfaceContainerHigh = CssVar("--emarsys-color-surface-container-high")
    val colorSurfaceContainerHighest = CssVar("--emarsys-color-surface-container-highest")
    val colorSurfaceContainerLow = CssVar("--emarsys-color-surface-container-low")
    val colorSurfaceContainerLowest = CssVar("--emarsys-color-surface-container-lowest")
    val colorSurfaceDim = CssVar("--emarsys-color-surface-dim")
    val colorSurfaceBright = CssVar("--emarsys-color-surface-bright")
    val colorSurfaceTint = CssVar("--emarsys-color-surface-tint")
    
    val colorOutline = CssVar("--emarsys-color-outline")
    val colorOutlineVariant = CssVar("--emarsys-color-outline-variant")
    
    val colorInverseSurface = CssVar("--emarsys-color-inverse-surface")
    val colorInverseOnSurface = CssVar("--emarsys-color-inverse-on-surface")
    val colorInversePrimary = CssVar("--emarsys-color-inverse-primary")
    
    val colorScrim = CssVar("--emarsys-color-scrim")

    val colorPrimaryFixed = CssVar("--emarsys-color-colorPrimaryFixed")
    val colorPrimaryFixedDim = CssVar("--emarsys-color-colorPrimaryFixedDim")
    val colorOnPrimaryFixed = CssVar("--emarsys-color-colorOnPrimaryFixed")
    val colorOnPrimaryFixedVariant = CssVar("--emarsys-color-colorOnPrimaryFixedVariant")
    val colorSecondaryFixed = CssVar("--emarsys-color-colorSecondaryFixed")
    val colorSecondaryFixedDim = CssVar("--emarsys-color-colorSecondaryFixedDim")
    val colorOnSecondaryFixed = CssVar("--emarsys-color-colorOnSecondaryFixed")
    val colorOnSecondaryFixedVariant = CssVar("--emarsys-color-colorOnSecondaryFixedVariant")
    val colorTertiaryFixed = CssVar("--emarsys-color-colorTertiaryFixed")
    val colorTertiaryFixedDim = CssVar("--emarsys-color-colorTertiaryFixedDim")
    val colorOnTertiaryFixed = CssVar("--emarsys-color-colorOnTertiaryFixed")
    val colorOnTertiaryFixedVariant = CssVar("--emarsys-color-colorOnTertiaryFixedVariant")
    
    val fontSizeDisplayLarge = CssVar("--emarsys-font-size-display-large")
    val fontSizeDisplayMedium = CssVar("--emarsys-font-size-display-medium")
    val fontSizeDisplaySmall = CssVar("--emarsys-font-size-display-small")
    val fontSizeHeadlineLarge = CssVar("--emarsys-font-size-headline-large")
    val fontSizeHeadlineMedium = CssVar("--emarsys-font-size-headline-medium")
    val fontSizeHeadlineSmall = CssVar("--emarsys-font-size-headline-small")
    val fontSizeTitleLarge = CssVar("--emarsys-font-size-title-large")
    val fontSizeTitleMedium = CssVar("--emarsys-font-size-title-medium")
    val fontSizeTitleSmall = CssVar("--emarsys-font-size-title-small")
    val fontSizeBodyLarge = CssVar("--emarsys-font-size-body-large")
    val fontSizeBodyMedium = CssVar("--emarsys-font-size-body-medium")
    val fontSizeBodySmall = CssVar("--emarsys-font-size-body-small")
    val fontSizeLabelLarge = CssVar("--emarsys-font-size-label-large")
    val fontSizeLabelMedium = CssVar("--emarsys-font-size-label-medium")
    val fontSizeLabelSmall = CssVar("--emarsys-font-size-label-small")
}
