package com.emarsys.mobileengage.embeddedmessaging.ui.theme

import androidx.compose.runtime.Composable
import com.emarsys.mobileengage.embeddedmessaging.EmbeddedMessagingContextApi
import com.emarsys.networking.clients.embedded.messaging.model.TextMetaData
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Style

internal class ThemeMapper(private val embeddedMessagingContext: EmbeddedMessagingContextApi) {

    @Composable
    fun generateThemeCSS() {
        val design = embeddedMessagingContext.metaData?.design
        mapFontSizes(design?.text)

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
                    setVar(CssColorVars.colorPrimary, "#526525")
                    setVar(CssColorVars.colorOnPrimary, "#ffffff")
                    setVar(CssColorVars.colorPrimaryContainer, "#9cbd4c")
                    setVar(CssColorVars.colorOnPrimaryContainer, "#000000")
                    setVar(CssColorVars.colorSecondary, "#365e2c")
                    setVar(CssColorVars.colorOnSecondary, "#ffffff")
                    setVar(CssColorVars.colorSecondaryContainer, "#DFE6C5")
                    setVar(CssColorVars.colorOnSecondaryContainer, "#000000")
                    setVar(CssColorVars.colorTertiary, "#6f6334")
                    setVar(CssColorVars.colorOnTertiary, "#ffffff")
                    setVar(CssColorVars.colorTertiaryContainer, "#b19f58")
                    setVar(CssColorVars.colorOnTertiaryContainer, "#000000")
                    setVar(CssColorVars.colorError, "#BA1A1A")
                    setVar(CssColorVars.colorOnError, "#FFFFFF")
                    setVar(CssColorVars.colorErrorContainer, "#FFDAD6")
                    setVar(CssColorVars.colorOnErrorContainer, "#410002")
                    setVar(CssColorVars.colorBackground, "#FDFCF6")
                    setVar(CssColorVars.colorOnBackground, "#1B1C18")
                    setVar(CssColorVars.colorSurface, "#FAFAEE")
                    setVar(CssColorVars.colorOnSurface, "#1B1C18")
                    setVar(CssColorVars.colorSurfaceVariant, "#EFEFE2")
                    setVar(CssColorVars.colorOnSurfaceVariant, "#45483C")
                    setVar(CssColorVars.colorSurfaceContainer, "#F0EFE8")
                    setVar(CssColorVars.colorSurfaceContainerHigh, "#EAE9E2")
                    setVar(CssColorVars.colorSurfaceContainerHighest, "#E4E3DD")
                    setVar(CssColorVars.colorSurfaceContainerLow, "#F6F5EE")
                    setVar(CssColorVars.colorSurfaceContainerLowest, "#FFFFFF")
                    setVar(CssColorVars.colorSurfaceDim, "#DDD9D1")
                    setVar(CssColorVars.colorSurfaceBright, "#FDFCF6")
                    setVar(CssColorVars.colorOutline, "#76786B")
                    setVar(CssColorVars.colorOutlineVariant, "#C6C8B8")
                    setVar(CssColorVars.colorInverseSurface, "#F2F1E9")
                    setVar(CssColorVars.colorInverseOnSurface, "#f9fbf4")
                    setVar(CssColorVars.colorInversePrimary, "#000000")
                    setVar(CssColorVars.colorScrim, "#526525")
                    setVar(CssColorVars.colorSurfaceTint, "#526525")
                    setVar(CssColorVars.colorPrimaryFixed, "#ffffff")
                    setVar(CssColorVars.colorPrimaryFixedDim, "#e4edcf")
                    setVar(CssColorVars.colorOnPrimaryFixed, "#000000")
                    setVar(CssColorVars.colorOnPrimaryFixedVariant, "#151a0a")
                    setVar(CssColorVars.colorSecondaryFixed, "#ffffff")
                    setVar(CssColorVars.colorSecondaryFixedDim, "#d7ead2")
                    setVar(CssColorVars.colorOnSecondaryFixed, "#000000")
                    setVar(CssColorVars.colorOnSecondaryFixedVariant, "#0e190b")
                    setVar(CssColorVars.colorTertiaryFixed, "#ffffff")
                    setVar(CssColorVars.colorTertiaryFixedDim, "#eae5d2")
                    setVar(CssColorVars.colorOnTertiaryFixed, "#000000")
                    setVar(CssColorVars.colorOnTertiaryFixedVariant, "#19160b")
                }
            }
        }
    }

    private fun mapFontSizes(typography: TextMetaData?) {
        CssFontVars.fontSizeDisplayLarge =
            typography?.displayLargeFontSize?.toInt()?.px ?: DefaultFontVars.fontSizeDisplayLarge
        CssFontVars.fontSizeDisplayMedium =
            typography?.displayMediumFontSize?.toInt()?.px ?: DefaultFontVars.fontSizeDisplayMedium
        CssFontVars.fontSizeDisplaySmall =
            typography?.displaySmallFontSize?.toInt()?.px ?: DefaultFontVars.fontSizeDisplaySmall
        CssFontVars.fontSizeHeadlineLarge =
            typography?.headlineLargeFontSize?.toInt()?.px ?: DefaultFontVars.fontSizeHeadlineLarge
        CssFontVars.fontSizeHeadlineMedium = typography?.headlineMediumFontSize?.toInt()?.px
            ?: DefaultFontVars.fontSizeHeadlineMedium
        CssFontVars.fontSizeHeadlineSmall =
            typography?.headlineSmallFontSize?.toInt()?.px ?: DefaultFontVars.fontSizeHeadlineSmall
        CssFontVars.fontSizeTitleLarge =
            typography?.titleLargeFontSize?.toInt()?.px ?: DefaultFontVars.fontSizeTitleLarge
        CssFontVars.fontSizeTitleMedium =
            typography?.titleMediumFontSize?.toInt()?.px ?: DefaultFontVars.fontSizeTitleMedium
        CssFontVars.fontSizeTitleSmall =
            typography?.titleSmallFontSize?.toInt()?.px ?: DefaultFontVars.fontSizeTitleSmall
        CssFontVars.fontSizeBodyLarge =
            typography?.bodyLargeFontSize?.toInt()?.px ?: DefaultFontVars.fontSizeBodyLarge
        CssFontVars.fontSizeBodyMedium =
            typography?.bodyMediumFontSize?.toInt()?.px ?: DefaultFontVars.fontSizeBodyMedium
        CssFontVars.fontSizeBodySmall =
            typography?.bodySmallFontSize?.toInt()?.px ?: DefaultFontVars.fontSizeBodySmall
        CssFontVars.fontSizeLabelLarge =
            typography?.labelLargeFontSize?.toInt()?.px ?: DefaultFontVars.fontSizeLabelLarge
        CssFontVars.fontSizeLabelMedium =
            typography?.labelMediumFontSize?.toInt()?.px ?: DefaultFontVars.fontSizeLabelMedium
        CssFontVars.fontSizeLabelSmall =
            typography?.labelSmallFontSize?.toInt()?.px ?: DefaultFontVars.fontSizeLabelSmall
    }
}