package com.sap.ec.mobileengage.embeddedmessaging.ui.theme

import androidx.compose.runtime.Composable
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.DEFAULT_ELEVATION
import com.sap.ec.networking.clients.embedded.messaging.model.MetaData
import com.sap.ec.networking.clients.embedded.messaging.model.Misc
import com.sap.ec.networking.clients.embedded.messaging.model.ShapesData
import com.sap.ec.networking.clients.embedded.messaging.model.TextMetaData
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Style
import kotlin.math.roundToInt

internal class ThemeMapper(private val metaData: MetaData?) {

    @Composable
    fun generateThemeCSS() {
        val design = metaData?.design
        mapFontSizes(design?.text)
        mapShapes(design?.shapes)
        mapMiscStyles(design?.misc)

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

    fun mapMisc(metaData: MetaData?): EmbeddedMessagingDesignValues {
        val misc = metaData?.design?.misc

        return misc?.let {
            EmbeddedMessagingDesignValues(
                messageItemMargin = it.messageItemMargin.roundToInt().px,
                messageItemElevation = it.messageItemElevation.roundToInt().px,
                buttonElevation = it.buttonElevation.roundToInt().px,
                listContentPadding = it.listContentPadding.roundToInt().px,
                listItemSpacing = it.listItemSpacing.roundToInt().px,
                compactOverlayWidth = it.compactOverlayWidth.roundToInt().px,
                compactOverlayMaxHeight = it.compactOverlayMaxHeight.roundToInt().px,
                compactOverlayCornerRadius = it.compactOverlayCornerRadius.roundToInt().px,
                compactOverlayElevation = it.compactOverlayElevation.roundToInt().px,
                emptyStateImageUrl = it.emptyStateImageUrl,
                messageItemImageHeight = it.messageItemImageHeight.roundToInt().px,
                messageItemImageClipShape = it.messageItemImageClipShape,
                messageItemImageCornerRadius = it.messageItemImageCornerRadius.roundToInt().px,
                messageItemCustomShape = it.messageItemCustomShape
            )
        } ?: EmbeddedMessagingDesignValues()
    }

    private fun mapMiscStyles(misc: Misc?) {
        CssMiscVars.buttonBoxShadow = "0 2px ${misc?.buttonElevation?.roundToInt() ?: DEFAULT_ELEVATION} rgba(0,0,0,0.1)"
    }

    private fun mapShapes(shapes: ShapesData?) {
        shapes?.let {
            CssShapeVars.extraSmallTopStart = it.extraSmall.topStart.roundToInt().px
            CssShapeVars.extraSmallTopEnd = it.extraSmall.topEnd.roundToInt().px
            CssShapeVars.extraSmallBottomStart = it.extraSmall.bottomStart.roundToInt().px
            CssShapeVars.extraSmallBottomEnd = it.extraSmall.bottomEnd.roundToInt().px

            CssShapeVars.smallTopStart = it.small.topStart.roundToInt().px
            CssShapeVars.smallTopEnd = it.small.topEnd.roundToInt().px
            CssShapeVars.smallBottomStart = it.small.bottomStart.roundToInt().px
            CssShapeVars.smallBottomEnd = it.small.bottomEnd.roundToInt().px

            CssShapeVars.mediumTopStart = it.medium.topStart.roundToInt().px
            CssShapeVars.mediumTopEnd = it.medium.topEnd.roundToInt().px
            CssShapeVars.mediumBottomStart = it.medium.bottomStart.roundToInt().px
            CssShapeVars.mediumBottomEnd = it.medium.bottomEnd.roundToInt().px

            CssShapeVars.largeTopStart = it.large.topStart.roundToInt().px
            CssShapeVars.largeTopEnd = it.large.topEnd.roundToInt().px
            CssShapeVars.largeBottomStart = it.large.bottomStart.roundToInt().px
            CssShapeVars.largeBottomEnd = it.large.bottomEnd.roundToInt().px

            CssShapeVars.extraLargeTopStart = it.extraLarge.topStart.roundToInt().px
            CssShapeVars.extraLargeTopEnd = it.extraLarge.topEnd.roundToInt().px
            CssShapeVars.extraLargeBottomStart = it.extraLarge.bottomStart.roundToInt().px
            CssShapeVars.extraLargeBottomEnd = it.extraLarge.bottomEnd.roundToInt().px
        }
    }

    private fun mapFontSizes(typography: TextMetaData?) {
        CssFontVars.fontSizeDisplayLarge =
            typography?.displayLargeFontSize?.roundToInt()?.px ?: DefaultFontVars.fontSizeDisplayLarge
        CssFontVars.fontSizeDisplayMedium =
            typography?.displayMediumFontSize?.roundToInt()?.px ?: DefaultFontVars.fontSizeDisplayMedium
        CssFontVars.fontSizeDisplaySmall =
            typography?.displaySmallFontSize?.roundToInt()?.px ?: DefaultFontVars.fontSizeDisplaySmall
        CssFontVars.fontSizeHeadlineLarge =
            typography?.headlineLargeFontSize?.roundToInt()?.px ?: DefaultFontVars.fontSizeHeadlineLarge
        CssFontVars.fontSizeHeadlineMedium = typography?.headlineMediumFontSize?.roundToInt()?.px
            ?: DefaultFontVars.fontSizeHeadlineMedium
        CssFontVars.fontSizeHeadlineSmall =
            typography?.headlineSmallFontSize?.roundToInt()?.px ?: DefaultFontVars.fontSizeHeadlineSmall
        CssFontVars.fontSizeTitleLarge =
            typography?.titleLargeFontSize?.roundToInt()?.px ?: DefaultFontVars.fontSizeTitleLarge
        CssFontVars.fontSizeTitleMedium =
            typography?.titleMediumFontSize?.roundToInt()?.px ?: DefaultFontVars.fontSizeTitleMedium
        CssFontVars.fontSizeTitleSmall =
            typography?.titleSmallFontSize?.roundToInt()?.px ?: DefaultFontVars.fontSizeTitleSmall
        CssFontVars.fontSizeBodyLarge =
            typography?.bodyLargeFontSize?.roundToInt()?.px ?: DefaultFontVars.fontSizeBodyLarge
        CssFontVars.fontSizeBodyMedium =
            typography?.bodyMediumFontSize?.roundToInt()?.px ?: DefaultFontVars.fontSizeBodyMedium
        CssFontVars.fontSizeBodySmall =
            typography?.bodySmallFontSize?.roundToInt()?.px ?: DefaultFontVars.fontSizeBodySmall
        CssFontVars.fontSizeLabelLarge =
            typography?.labelLargeFontSize?.roundToInt()?.px ?: DefaultFontVars.fontSizeLabelLarge
        CssFontVars.fontSizeLabelMedium =
            typography?.labelMediumFontSize?.roundToInt()?.px ?: DefaultFontVars.fontSizeLabelMedium
        CssFontVars.fontSizeLabelSmall =
            typography?.labelSmallFontSize?.roundToInt()?.px ?: DefaultFontVars.fontSizeLabelSmall
    }
}