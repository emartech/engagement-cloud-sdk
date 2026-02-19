package com.sap.ec.mobileengage.embeddedmessaging.ui.theme

import androidx.compose.runtime.Composable
import com.sap.ec.mobileengage.embeddedmessaging.EmbeddedMessagingContextApi
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants
import com.sap.ec.networking.clients.embedded.messaging.model.TextMetaData
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Style

internal class ThemeMapper(private val embeddedMessagingContext: EmbeddedMessagingContextApi) {

    @Composable
    fun generateThemeCSS() {
        val design = embeddedMessagingContext.metaData?.design
        mapFontSizes(design?.text)

        val colors = design?.fillColor

        Style {
            ":root" style {
                setVar(
                    CssColorVars.colorPrimary,
                    colors?.primary ?: EmbeddedMessagingConstants.ColorDefaults.PRIMARY
                )
                setVar(
                    CssColorVars.colorOnPrimary,
                    colors?.onPrimary ?: EmbeddedMessagingConstants.ColorDefaults.ON_PRIMARY
                )
                setVar(
                    CssColorVars.colorPrimaryContainer,
                    colors?.primaryContainer
                        ?: EmbeddedMessagingConstants.ColorDefaults.PRIMARY_CONTAINER
                )
                setVar(
                    CssColorVars.colorOnPrimaryContainer,
                    colors?.onPrimaryContainer
                        ?: EmbeddedMessagingConstants.ColorDefaults.ON_PRIMARY_CONTAINER
                )
                setVar(
                    CssColorVars.colorSecondary,
                    colors?.secondary ?: EmbeddedMessagingConstants.ColorDefaults.SECONDARY
                )
                setVar(
                    CssColorVars.colorOnSecondary,
                    colors?.onSecondary ?: EmbeddedMessagingConstants.ColorDefaults.ON_SECONDARY
                )
                setVar(
                    CssColorVars.colorSecondaryContainer,
                    colors?.secondaryContainer
                        ?: EmbeddedMessagingConstants.ColorDefaults.SECONDARY_CONTAINER
                )
                setVar(
                    CssColorVars.colorOnSecondaryContainer,
                    colors?.onSecondaryContainer
                        ?: EmbeddedMessagingConstants.ColorDefaults.ON_SECONDARY_CONTAINER
                )
                setVar(
                    CssColorVars.colorTertiary,
                    colors?.tertiary ?: EmbeddedMessagingConstants.ColorDefaults.TERTIARY
                )
                setVar(
                    CssColorVars.colorOnTertiary,
                    colors?.onTertiary ?: EmbeddedMessagingConstants.ColorDefaults.ON_TERTIARY
                )
                setVar(
                    CssColorVars.colorTertiaryContainer,
                    colors?.tertiaryContainer
                        ?: EmbeddedMessagingConstants.ColorDefaults.TERTIARY_CONTAINER
                )
                setVar(
                    CssColorVars.colorOnTertiaryContainer,
                    colors?.onTertiaryContainer
                        ?: EmbeddedMessagingConstants.ColorDefaults.ON_TERTIARY_CONTAINER
                )
                setVar(
                    CssColorVars.colorError,
                    colors?.error ?: EmbeddedMessagingConstants.ColorDefaults.ERROR
                )
                setVar(
                    CssColorVars.colorOnError,
                    colors?.onError ?: EmbeddedMessagingConstants.ColorDefaults.ON_ERROR
                )
                setVar(
                    CssColorVars.colorErrorContainer,
                    colors?.errorContainer
                        ?: EmbeddedMessagingConstants.ColorDefaults.ERROR_CONTAINER
                )
                setVar(
                    CssColorVars.colorOnErrorContainer,
                    colors?.onErrorContainer
                        ?: EmbeddedMessagingConstants.ColorDefaults.ON_ERROR_CONTAINER
                )
                setVar(
                    CssColorVars.colorBackground,
                    colors?.background ?: EmbeddedMessagingConstants.ColorDefaults.BACKGROUND
                )
                setVar(
                    CssColorVars.colorOnBackground,
                    colors?.onBackground ?: EmbeddedMessagingConstants.ColorDefaults.ON_BACKGROUND
                )
                setVar(
                    CssColorVars.colorSurface,
                    colors?.surface ?: EmbeddedMessagingConstants.ColorDefaults.SURFACE
                )
                setVar(
                    CssColorVars.colorOnSurface,
                    colors?.onSurface ?: EmbeddedMessagingConstants.ColorDefaults.ON_SURFACE
                )
                setVar(
                    CssColorVars.colorSurfaceVariant,
                    colors?.surfaceVariant
                        ?: EmbeddedMessagingConstants.ColorDefaults.SURFACE_VARIANT
                )
                setVar(
                    CssColorVars.colorOnSurfaceVariant,
                    colors?.onSurfaceVariant
                        ?: EmbeddedMessagingConstants.ColorDefaults.ON_SURFACE_VARIANT
                )
                setVar(
                    CssColorVars.colorSurfaceContainer,
                    colors?.surfaceContainer
                        ?: EmbeddedMessagingConstants.ColorDefaults.SURFACE_CONTAINER
                )
                setVar(
                    CssColorVars.colorSurfaceContainerHigh,
                    colors?.surfaceContainerHigh
                        ?: EmbeddedMessagingConstants.ColorDefaults.SURFACE_CONTAINER_HIGH
                )
                setVar(
                    CssColorVars.colorSurfaceContainerHighest,
                    colors?.surfaceContainerHighest
                        ?: EmbeddedMessagingConstants.ColorDefaults.SURFACE_CONTAINER_HIGHEST
                )
                setVar(
                    CssColorVars.colorSurfaceContainerLow,
                    colors?.surfaceContainerLow
                        ?: EmbeddedMessagingConstants.ColorDefaults.SURFACE_CONTAINER_LOW
                )
                setVar(
                    CssColorVars.colorSurfaceContainerLowest,
                    colors?.surfaceContainerLowest
                        ?: EmbeddedMessagingConstants.ColorDefaults.SURFACE_CONTAINER_LOWEST
                )
                setVar(
                    CssColorVars.colorSurfaceDim,
                    colors?.surfaceDim ?: EmbeddedMessagingConstants.ColorDefaults.SURFACE_DIM
                )
                setVar(
                    CssColorVars.colorSurfaceBright,
                    colors?.surfaceBright ?: EmbeddedMessagingConstants.ColorDefaults.SURFACE_BRIGHT
                )
                setVar(
                    CssColorVars.colorOutline,
                    colors?.outline ?: EmbeddedMessagingConstants.ColorDefaults.OUTLINE
                )
                setVar(
                    CssColorVars.colorOutlineVariant,
                    colors?.outlineVariant
                        ?: EmbeddedMessagingConstants.ColorDefaults.OUTLINE_VARIANT
                )
                setVar(
                    CssColorVars.colorInverseSurface,
                    colors?.inverseSurface
                        ?: EmbeddedMessagingConstants.ColorDefaults.INVERSE_SURFACE
                )
                setVar(
                    CssColorVars.colorInverseOnSurface,
                    colors?.inverseOnSurface
                        ?: EmbeddedMessagingConstants.ColorDefaults.INVERSE_ON_SURFACE
                )
                setVar(
                    CssColorVars.colorInversePrimary,
                    colors?.inversePrimary
                        ?: EmbeddedMessagingConstants.ColorDefaults.INVERSE_PRIMARY
                )
                setVar(
                    CssColorVars.colorScrim,
                    colors?.scrim ?: EmbeddedMessagingConstants.ColorDefaults.SCRIM
                )
                setVar(
                    CssColorVars.colorSurfaceTint,
                    colors?.surfaceTint ?: EmbeddedMessagingConstants.ColorDefaults.SURFACE_TINT
                )
                setVar(
                    CssColorVars.colorPrimaryFixed,
                    colors?.primaryFixed ?: EmbeddedMessagingConstants.ColorDefaults.PRIMARY_FIXED
                )
                setVar(
                    CssColorVars.colorPrimaryFixedDim,
                    colors?.primaryFixedDim
                        ?: EmbeddedMessagingConstants.ColorDefaults.PRIMARY_FIXED_DIM
                )
                setVar(
                    CssColorVars.colorOnPrimaryFixed,
                    colors?.onPrimaryFixed
                        ?: EmbeddedMessagingConstants.ColorDefaults.ON_PRIMARY_FIXED
                )
                setVar(
                    CssColorVars.colorOnPrimaryFixedVariant,
                    colors?.onPrimaryFixedVariant
                        ?: EmbeddedMessagingConstants.ColorDefaults.ON_PRIMARY_FIXED_VARIANT
                )
                setVar(
                    CssColorVars.colorSecondaryFixed,
                    colors?.secondaryFixed
                        ?: EmbeddedMessagingConstants.ColorDefaults.SECONDARY_FIXED
                )
                setVar(
                    CssColorVars.colorSecondaryFixedDim,
                    colors?.secondaryFixedDim
                        ?: EmbeddedMessagingConstants.ColorDefaults.SECONDARY_FIXED_DIM
                )
                setVar(
                    CssColorVars.colorOnSecondaryFixed,
                    colors?.onSecondaryFixed
                        ?: EmbeddedMessagingConstants.ColorDefaults.ON_SECONDARY_FIXED
                )
                setVar(
                    CssColorVars.colorOnSecondaryFixedVariant,
                    colors?.onSecondaryFixedVariant
                        ?: EmbeddedMessagingConstants.ColorDefaults.ON_SECONDARY_FIXED_VARIANT
                )
                setVar(
                    CssColorVars.colorTertiaryFixed,
                    colors?.tertiaryFixed ?: EmbeddedMessagingConstants.ColorDefaults.TERTIARY_FIXED
                )
                setVar(
                    CssColorVars.colorTertiaryFixedDim,
                    colors?.tertiaryFixedDim
                        ?: EmbeddedMessagingConstants.ColorDefaults.TERTIARY_FIXED_DIM
                )
                setVar(
                    CssColorVars.colorOnTertiaryFixed,
                    colors?.onTertiaryFixed
                        ?: EmbeddedMessagingConstants.ColorDefaults.ON_TERTIARY_FIXED
                )
                setVar(
                    CssColorVars.colorOnTertiaryFixedVariant,
                    colors?.onTertiaryFixedVariant
                        ?: EmbeddedMessagingConstants.ColorDefaults.ON_TERTIARY_FIXED_VARIANT
                )
            }
        }
    }

    fun mapMisc(): EmbeddedMessagingDesignValues {
        return EmbeddedMessagingDesignValues()
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