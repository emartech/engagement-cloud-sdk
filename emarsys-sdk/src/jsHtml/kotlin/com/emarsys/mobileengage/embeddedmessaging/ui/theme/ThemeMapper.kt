package com.emarsys.mobileengage.embeddedmessaging.ui.theme

import androidx.compose.runtime.Composable
import com.emarsys.mobileengage.embeddedmessaging.EmbeddedMessagingContextApi
import org.jetbrains.compose.web.dom.Style

internal class ThemeMapper(private val embeddedMessagingContext: EmbeddedMessagingContextApi) {

    @Composable
    fun generateThemeCSS() {
        val design = embeddedMessagingContext.metaData?.design

//        if (design != null) {
//            val colors = design.fillColor
//            val typography = design.text
//
//            Style {
//                ":root" style {
//                    // Colors - Primary
//                    setVar(CssVars.colorPrimary, colors?.primary ?: "#1565C0")
//                    setVar(CssVars.colorOnPrimary, colors?.onPrimary ?: "#FFFFFF")
//                    setVar(CssVars.colorPrimaryContainer, colors?.primaryContainer ?: "#BBDEFB")
//                    setVar(CssVars.colorOnPrimaryContainer, colors?.onPrimaryContainer ?: "#0D47A1")
//
//                    // Colors - Secondary
//                    setVar(CssVars.colorSecondary, colors?.secondary ?: "#5C6BC0")
//                    setVar(CssVars.colorOnSecondary, colors?.onSecondary ?: "#FFFFFF")
//                    setVar(CssVars.colorSecondaryContainer, colors?.secondaryContainer ?: "#E8EAF6")
//                    setVar(CssVars.colorOnSecondaryContainer, colors?.onSecondaryContainer ?: "#283593")
//
//                    // Colors - Tertiary
//                    setVar(CssVars.colorTertiary, colors?.tertiary ?: "#00ACC1")
//                    setVar(CssVars.colorOnTertiary, colors?.onTertiary ?: "#FFFFFF")
//                    setVar(CssVars.colorTertiaryContainer, colors?.tertiaryContainer ?: "#B2EBF2")
//                    setVar(CssVars.colorOnTertiaryContainer, colors?.onTertiaryContainer ?: "#006064")
//
//                    // Colors - Error
//                    setVar(CssVars.colorError, colors?.error ?: "#E53935")
//                    setVar(CssVars.colorOnError, colors?.onError ?: "#FFFFFF")
//                    setVar(CssVars.colorErrorContainer, colors?.errorContainer ?: "#FFEBEE")
//                    setVar(CssVars.colorOnErrorContainer, colors?.onErrorContainer ?: "#B71C1C")
//
//                    // Colors - Background & Surface
//                    setVar(CssVars.colorBackground, colors?.background ?: "#F8FAFC")
//                    setVar(CssVars.colorOnBackground, colors?.onBackground ?: "#1E293B")
//                    setVar(CssVars.colorSurface, colors?.surface ?: "#FFFFFF")
//                    setVar(CssVars.colorOnSurface, colors?.onSurface ?: "#334155")
//                    setVar(CssVars.colorSurfaceVariant, colors?.surfaceVariant ?: "#F1F5F9")
//                    setVar(CssVars.colorOnSurfaceVariant, colors?.onSurfaceVariant ?: "#64748B")
//
//                    // Colors - Surface Containers
//                    setVar(CssVars.colorSurfaceContainer, colors?.surfaceContainer ?: "#F8FAFC")
//                    setVar(CssVars.colorSurfaceContainerHigh, colors?.surfaceContainerHigh ?: "#F1F5F9")
//                    setVar(CssVars.colorSurfaceContainerHighest, colors?.surfaceContainerHighest ?: "#E2E8F0")
//                    setVar(CssVars.colorSurfaceContainerLow, colors?.surfaceContainerLow ?: "#FAFBFC")
//                    setVar(CssVars.colorSurfaceContainerLowest, colors?.surfaceContainerLowest ?: "#FFFFFF")
//                    setVar(CssVars.colorSurfaceDim, colors?.surfaceDim ?: "#E2E8F0")
//                    setVar(CssVars.colorSurfaceBright, colors?.surfaceBright ?: "#FFFFFF")
//                    setVar(CssVars.colorSurfaceTint, colors?.surfaceTint ?: "#1565C0")
//
//                    // Colors - Outline
//                    setVar(CssVars.colorOutline, colors?.outline ?: "#CBD5E1")
//                    setVar(CssVars.colorOutlineVariant, colors?.outlineVariant ?: "#E2E8F0")
//
//                    // Colors - Inverse
//                    setVar(CssVars.colorInverseSurface, colors?.inverseSurface ?: "#1E293B")
//                    setVar(CssVars.colorInverseOnSurface, colors?.inverseOnSurface ?: "#F8FAFC")
//                    setVar(CssVars.colorInversePrimary, colors?.inversePrimary ?: "#90CAF9")
//
//                    // Colors - Other
//                    setVar(CssVars.colorScrim, colors?.scrim ?: "#000000")
//
//                    // Typography
//                    setVar(CssVars.fontSizeDisplayLarge, "${typography?.displayLargeFontSize?.toInt() ?: 56}px")
//                    setVar(CssVars.fontSizeDisplayMedium, "${typography?.displayMediumFontSize?.toInt() ?: 44}px")
//                    setVar(CssVars.fontSizeDisplaySmall, "${typography?.displaySmallFontSize?.toInt() ?: 36}px")
//                    setVar(CssVars.fontSizeHeadlineLarge, "${typography?.headlineLargeFontSize?.toInt() ?: 32}px")
//                    setVar(CssVars.fontSizeHeadlineMedium, "${typography?.headlineMediumFontSize?.toInt() ?: 28}px")
//                    setVar(CssVars.fontSizeHeadlineSmall, "${typography?.headlineSmallFontSize?.toInt() ?: 24}px")
//                    setVar(CssVars.fontSizeTitleLarge, "${typography?.titleLargeFontSize?.toInt() ?: 20}px")
//                    setVar(CssVars.fontSizeTitleMedium, "${typography?.titleMediumFontSize?.toInt() ?: 16}px")
//                    setVar(CssVars.fontSizeTitleSmall, "${typography?.titleSmallFontSize?.toInt() ?: 14}px")
//                    setVar(CssVars.fontSizeBodyLarge, "${typography?.bodyLargeFontSize?.toInt() ?: 16}px")
//                    setVar(CssVars.fontSizeBodyMedium, "${typography?.bodyMediumFontSize?.toInt() ?: 14}px")
//                    setVar(CssVars.fontSizeBodySmall, "${typography?.bodySmallFontSize?.toInt() ?: 12}px")
//                    setVar(CssVars.fontSizeLabelLarge, "${typography?.labelLargeFontSize?.toInt() ?: 14}px")
//                    setVar(CssVars.fontSizeLabelMedium, "${typography?.labelMediumFontSize?.toInt() ?: 12}px")
//                    setVar(CssVars.fontSizeLabelSmall, "${typography?.labelSmallFontSize?.toInt() ?: 10}px")
//                }
//            }
//        } else {
            generateDefaultThemeCSS()
//        }
    }

    fun mapMisc(): EmbeddedMessagingDesignValues {
        return EmbeddedMessagingDesignValues()
    }

    companion object {
        @Composable
        fun generateDefaultThemeCSS() {
            Style {
                ":root" style {
                    setVar(CssVars.colorPrimary, "#526525")
                    setVar(CssVars.colorOnPrimary, "#ffffff")
                    setVar(CssVars.colorPrimaryContainer, "#9cbd4c")
                    setVar(CssVars.colorOnPrimaryContainer, "#000000")
                    setVar(CssVars.colorSecondary, "#365e2c")
                    setVar(CssVars.colorOnSecondary, "#ffffff")
                    setVar(CssVars.colorSecondaryContainer, "#DFE6C5")
                    setVar(CssVars.colorOnSecondaryContainer, "#000000")
                    setVar(CssVars.colorTertiary, "#6f6334")
                    setVar(CssVars.colorOnTertiary, "#ffffff")
                    setVar(CssVars.colorTertiaryContainer, "#b19f58")
                    setVar(CssVars.colorOnTertiaryContainer, "#000000")
                    setVar(CssVars.colorError, "#BA1A1A")
                    setVar(CssVars.colorOnError, "#FFFFFF")
                    setVar(CssVars.colorErrorContainer, "#FFDAD6")
                    setVar(CssVars.colorOnErrorContainer, "#410002")
                    setVar(CssVars.colorBackground, "#FDFCF6")
                    setVar(CssVars.colorOnBackground, "#1B1C18")
                    setVar(CssVars.colorSurface, "#FAFAEE")
                    setVar(CssVars.colorOnSurface, "#1B1C18")
                    setVar(CssVars.colorSurfaceVariant, "#EFEFE2")
                    setVar(CssVars.colorOnSurfaceVariant, "#45483C")
                    setVar(CssVars.colorSurfaceContainer, "#F0EFE8")
                    setVar(CssVars.colorSurfaceContainerHigh, "#EAE9E2")
                    setVar(CssVars.colorSurfaceContainerHighest, "#E4E3DD")
                    setVar(CssVars.colorSurfaceContainerLow, "#F6F5EE")
                    setVar(CssVars.colorSurfaceContainerLowest, "#FFFFFF")
                    setVar(CssVars.colorSurfaceDim, "#DDD9D1")
                    setVar(CssVars.colorSurfaceBright, "#FDFCF6")
                    setVar(CssVars.colorOutline, "#ffffff")
                    setVar(CssVars.colorOutlineVariant, "#30312C")
                    setVar(CssVars.colorInverseSurface, "#F2F1E9")
                    setVar(CssVars.colorInverseOnSurface, "#f9fbf4")
                    setVar(CssVars.colorInversePrimary, "#000000")
                    setVar(CssVars.colorScrim, "#526525")
                    setVar(CssVars.colorSurfaceTint, "#526525")
                    setVar(CssVars.colorPrimaryFixed, "#ffffff")
                    setVar(CssVars.colorPrimaryFixedDim, "#e4edcf")
                    setVar(CssVars.colorOnPrimaryFixed, "#000000")
                    setVar(CssVars.colorOnPrimaryFixedVariant, "#151a0a")
                    setVar(CssVars.colorSecondaryFixed, "#ffffff")
                    setVar(CssVars.colorSecondaryFixedDim, "#d7ead2")
                    setVar(CssVars.colorOnSecondaryFixed, "#000000")
                    setVar(CssVars.colorOnSecondaryFixedVariant, "#0e190b")
                    setVar(CssVars.colorTertiaryFixed, "#ffffff")
                    setVar(CssVars.colorTertiaryFixedDim, "#eae5d2")
                    setVar(CssVars.colorOnTertiaryFixed, "#000000")
                    setVar(CssVars.colorOnTertiaryFixedVariant, "#19160b")
                    setVar(CssVars.fontSizeDisplayLarge, "56px")
                    setVar(CssVars.fontSizeDisplayMedium, "44px")
                    setVar(CssVars.fontSizeDisplaySmall, "36px")
                    setVar(CssVars.fontSizeHeadlineLarge, "32px")
                    setVar(CssVars.fontSizeHeadlineMedium, "28px")
                    setVar(CssVars.fontSizeHeadlineSmall, "24px")
                    setVar(CssVars.fontSizeTitleLarge, "20px")
                    setVar(CssVars.fontSizeTitleMedium, "16px")
                    setVar(CssVars.fontSizeTitleSmall, "14px")
                    setVar(CssVars.fontSizeBodyLarge, "16px")
                    setVar(CssVars.fontSizeBodyMedium, "14px")
                    setVar(CssVars.fontSizeBodySmall, "12px")
                    setVar(CssVars.fontSizeLabelLarge, "14px")
                    setVar(CssVars.fontSizeLabelMedium, "12px")
                    setVar(CssVars.fontSizeLabelSmall, "10px")
                }
            }
        }
    }
}