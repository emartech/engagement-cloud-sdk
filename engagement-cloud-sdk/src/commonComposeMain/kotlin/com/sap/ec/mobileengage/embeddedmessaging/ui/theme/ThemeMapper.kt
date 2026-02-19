package com.sap.ec.mobileengage.embeddedmessaging.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sap.ec.mobileengage.embeddedmessaging.EmbeddedMessagingContextApi
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.Dimensions.DEFAULT_ELEVATION
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.Dimensions.ZERO_ELEVATION
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.Shapes.ZERO_CORNER_RADIUS

internal class ThemeMapper(private val embeddedMessagingContext: EmbeddedMessagingContextApi) {

    @Composable
    fun mapColorScheme(): ColorScheme {
        val colors = embeddedMessagingContext.metaData?.design?.fillColor
        return colors?.let {
            ColorScheme(
                primary = colors.primary.toColor(EmbeddedMessagingUiConstants.Colors.getDefaultColorScheme().primary),
                onPrimary = colors.onPrimary.toColor(EmbeddedMessagingUiConstants.Colors.getDefaultColorScheme().onPrimary),
                primaryContainer = colors.primaryContainer.toColor(EmbeddedMessagingUiConstants.Colors.getDefaultColorScheme().primaryContainer),
                onPrimaryContainer = colors.onPrimaryContainer.toColor(EmbeddedMessagingUiConstants.Colors.getDefaultColorScheme().onPrimaryContainer),
                secondary = colors.secondary.toColor(EmbeddedMessagingUiConstants.Colors.getDefaultColorScheme().secondary),
                onSecondary = colors.onSecondary.toColor(EmbeddedMessagingUiConstants.Colors.getDefaultColorScheme().onSecondary),
                secondaryContainer = colors.secondaryContainer.toColor(EmbeddedMessagingUiConstants.Colors.getDefaultColorScheme().secondaryContainer),
                onSecondaryContainer = colors.onSecondaryContainer.toColor(EmbeddedMessagingUiConstants.Colors.getDefaultColorScheme().onSecondaryContainer),
                tertiary = colors.tertiary.toColor(EmbeddedMessagingUiConstants.Colors.getDefaultColorScheme().tertiary),
                onTertiary = colors.onTertiary.toColor(EmbeddedMessagingUiConstants.Colors.getDefaultColorScheme().onTertiary),
                tertiaryContainer = colors.tertiaryContainer.toColor(EmbeddedMessagingUiConstants.Colors.getDefaultColorScheme().tertiaryContainer),
                onTertiaryContainer = colors.onTertiaryContainer.toColor(EmbeddedMessagingUiConstants.Colors.getDefaultColorScheme().onTertiaryContainer),
                error = colors.error.toColor(EmbeddedMessagingUiConstants.Colors.getDefaultColorScheme().error),
                onError = colors.onError.toColor(EmbeddedMessagingUiConstants.Colors.getDefaultColorScheme().onError),
                errorContainer = colors.errorContainer.toColor(EmbeddedMessagingUiConstants.Colors.getDefaultColorScheme().errorContainer),
                onErrorContainer = colors.onErrorContainer.toColor(EmbeddedMessagingUiConstants.Colors.getDefaultColorScheme().onErrorContainer),
                background = colors.background.toColor(EmbeddedMessagingUiConstants.Colors.getDefaultColorScheme().background),
                onBackground = colors.onBackground.toColor(EmbeddedMessagingUiConstants.Colors.getDefaultColorScheme().onBackground),
                surface = colors.surface.toColor(EmbeddedMessagingUiConstants.Colors.getDefaultColorScheme().surface),
                onSurface = colors.onSurface.toColor(EmbeddedMessagingUiConstants.Colors.getDefaultColorScheme().onSurface),
                surfaceVariant = colors.surfaceVariant.toColor(EmbeddedMessagingUiConstants.Colors.getDefaultColorScheme().surfaceVariant),
                onSurfaceVariant = colors.onSurfaceVariant.toColor(EmbeddedMessagingUiConstants.Colors.getDefaultColorScheme().onSurfaceVariant),
                surfaceContainer = colors.surfaceContainer.toColor(EmbeddedMessagingUiConstants.Colors.getDefaultColorScheme().surfaceContainer),
                surfaceContainerHigh = colors.surfaceContainerHigh.toColor(EmbeddedMessagingUiConstants.Colors.getDefaultColorScheme().surfaceContainerHigh),
                surfaceContainerHighest = colors.surfaceContainerHighest.toColor(EmbeddedMessagingUiConstants.Colors.getDefaultColorScheme().surfaceContainerHighest),
                surfaceContainerLow = colors.surfaceContainerLow.toColor(EmbeddedMessagingUiConstants.Colors.getDefaultColorScheme().surfaceContainerLow),
                surfaceContainerLowest = colors.surfaceContainerLowest.toColor(EmbeddedMessagingUiConstants.Colors.getDefaultColorScheme().surfaceContainerLowest),
                surfaceDim = colors.surfaceDim.toColor(EmbeddedMessagingUiConstants.Colors.getDefaultColorScheme().surfaceDim),
                surfaceBright = colors.surfaceBright.toColor(EmbeddedMessagingUiConstants.Colors.getDefaultColorScheme().surfaceBright),
                outline = colors.outline.toColor(EmbeddedMessagingUiConstants.Colors.getDefaultColorScheme().outline),
                outlineVariant = colors.outlineVariant.toColor(EmbeddedMessagingUiConstants.Colors.getDefaultColorScheme().outlineVariant),
                inverseSurface = colors.inverseSurface.toColor(EmbeddedMessagingUiConstants.Colors.getDefaultColorScheme().inverseSurface),
                inverseOnSurface = colors.inverseOnSurface.toColor(EmbeddedMessagingUiConstants.Colors.getDefaultColorScheme().inverseOnSurface),
                inversePrimary = colors.inversePrimary.toColor(EmbeddedMessagingUiConstants.Colors.getDefaultColorScheme().inversePrimary),
                scrim = colors.scrim.toColor(EmbeddedMessagingUiConstants.Colors.getDefaultColorScheme().scrim),
                surfaceTint = colors.surfaceTint.toColor(EmbeddedMessagingUiConstants.Colors.getDefaultColorScheme().surfaceTint),
                primaryFixed = colors.primaryFixed.toColor(EmbeddedMessagingUiConstants.Colors.getDefaultColorScheme().primaryFixed),
                primaryFixedDim = colors.primaryFixedDim.toColor(EmbeddedMessagingUiConstants.Colors.getDefaultColorScheme().primaryFixedDim),
                onPrimaryFixed = colors.onPrimaryFixed.toColor(EmbeddedMessagingUiConstants.Colors.getDefaultColorScheme().onPrimaryFixed),
                onPrimaryFixedVariant = colors.onPrimaryFixedVariant.toColor(EmbeddedMessagingUiConstants.Colors.getDefaultColorScheme().onPrimaryFixedVariant),
                secondaryFixed = colors.secondaryFixed.toColor(EmbeddedMessagingUiConstants.Colors.getDefaultColorScheme().secondaryFixed),
                secondaryFixedDim = colors.secondaryFixedDim.toColor(EmbeddedMessagingUiConstants.Colors.getDefaultColorScheme().secondaryFixedDim),
                onSecondaryFixed = colors.onSecondaryFixed.toColor(EmbeddedMessagingUiConstants.Colors.getDefaultColorScheme().onSecondaryFixed),
                onSecondaryFixedVariant = colors.onSecondaryFixedVariant.toColor(EmbeddedMessagingUiConstants.Colors.getDefaultColorScheme().onSecondaryFixedVariant),
                tertiaryFixed = colors.tertiaryFixed.toColor(EmbeddedMessagingUiConstants.Colors.getDefaultColorScheme().tertiaryFixed),
                tertiaryFixedDim = colors.tertiaryFixedDim.toColor(EmbeddedMessagingUiConstants.Colors.getDefaultColorScheme().tertiaryFixedDim),
                onTertiaryFixed = colors.onTertiaryFixed.toColor(EmbeddedMessagingUiConstants.Colors.getDefaultColorScheme().onTertiaryFixed),
                onTertiaryFixedVariant = colors.onTertiaryFixedVariant.toColor(EmbeddedMessagingUiConstants.Colors.getDefaultColorScheme().onTertiaryFixedVariant)
            )
        } ?: EmbeddedMessagingUiConstants.Colors.getDefaultColorScheme()
    }

    @Composable
    fun mapTypography(): Typography {
        val textMetaData = embeddedMessagingContext.metaData?.design?.text
        return textMetaData?.let {
            Typography(
                displayLarge = textMetaData.displayLargeFontSize.toTextStyle(),
                displayMedium = textMetaData.displayMediumFontSize.toTextStyle(),
                displaySmall = textMetaData.displaySmallFontSize.toTextStyle(),
                headlineLarge = textMetaData.headlineLargeFontSize.toTextStyle(),
                headlineMedium = textMetaData.headlineMediumFontSize.toTextStyle(),
                headlineSmall = textMetaData.headlineSmallFontSize.toTextStyle(),
                titleLarge = textMetaData.titleLargeFontSize.toTextStyle(),
                titleMedium = textMetaData.titleMediumFontSize.toTextStyle(),
                titleSmall = textMetaData.titleSmallFontSize.toTextStyle(),
                bodyLarge = textMetaData.bodyLargeFontSize.toTextStyle(),
                bodyMedium = textMetaData.bodyMediumFontSize.toTextStyle(),
                bodySmall = textMetaData.bodySmallFontSize.toTextStyle(),
                labelLarge = textMetaData.labelLargeFontSize.toTextStyle(),
                labelMedium = textMetaData.labelMediumFontSize.toTextStyle(),
                labelSmall = textMetaData.labelSmallFontSize.toTextStyle()
            )
        } ?: MaterialTheme.typography
    }

    @Composable
    fun mapShapes(): Shapes {
        val shapeMetaData = embeddedMessagingContext.metaData?.design?.shapes
        return shapeMetaData?.let {
            MaterialTheme.shapes.copy(
                extraSmall = RoundedCornerShape(
                    topStart = it.extraSmall.topStart.dp,
                    topEnd = it.extraSmall.topEnd.dp,
                    bottomStart = it.extraSmall.bottomStart.dp,
                    bottomEnd = it.extraSmall.bottomEnd.dp,
                ),
                small = RoundedCornerShape(
                    topStart = it.small.topStart.dp,
                    topEnd = it.small.topEnd.dp,
                    bottomStart = it.small.bottomStart.dp,
                    bottomEnd = it.small.bottomEnd.dp,
                ),
                medium = RoundedCornerShape(
                    topStart = it.medium.topStart.dp,
                    topEnd = it.medium.topEnd.dp,
                    bottomStart = it.medium.bottomStart.dp,
                    bottomEnd = it.medium.bottomEnd.dp,
                ),
                large = RoundedCornerShape(
                    topStart = it.large.topStart.dp,
                    topEnd = it.large.topEnd.dp,
                    bottomStart = it.large.bottomStart.dp,
                    bottomEnd = it.large.bottomEnd.dp,
                ),
                extraLarge = RoundedCornerShape(
                    topStart = it.extraLarge.topStart.dp,
                    topEnd = it.extraLarge.topEnd.dp,
                    bottomStart = it.extraLarge.bottomStart.dp,
                    bottomEnd = it.extraLarge.bottomEnd.dp,
                )
            )
        } ?: MaterialTheme.shapes
    }

    fun mapMisc(): EmbeddedMessagingDesignValues {
        val miscMetaData = embeddedMessagingContext.metaData?.design?.misc
        return miscMetaData?.let {
            EmbeddedMessagingDesignValues(
                messageItemMargin = it.messageItemMargin.dp,
                messageItemElevation = it.messageItemElevation.dp,
                buttonElevation = DEFAULT_ELEVATION,
                listContentPadding = it.listContentPadding.dp,
                listItemSpacing = it.listItemSpacing.dp,
                compactOverlayWidth = it.compactOverlayWidth.dp,
                compactOverlayMaxHeight = it.compactOverlayMaxHeight.dp,
                compactOverlayCornerRadius = it.compactOverlayCornerRadius.dp,
                compactOverlayElevation = it.compactOverlayElevation.dp,
                emptyStateImageUrl = it.emptyStateImageUrl,
                messageItemCardCornerRadius = if (it.messageItemCardCornerRadius != null) it.messageItemCardCornerRadius.dp else ZERO_CORNER_RADIUS,
                messageItemCardElevation = if (it.messageItemCardElevation != null) it.messageItemCardElevation.dp else ZERO_ELEVATION,
                messageItemImageHeight = it.messageItemImageHeight.dp,
                messageItemImageClipShape = it.messageItemImageClipShape,
                messageItemImageCornerRadius = it.messageItemImageCornerRadius.dp,
                messageItemCustomShape = it.messageItemCustomShape
            )
        } ?: EmbeddedMessagingDesignValues()
    }


    private fun Double.toTextStyle(): TextStyle {
        return TextStyle(fontSize = this.sp)
    }
}

internal fun String.toColor(fallback: Color): Color {
    return try {
        val colorString = this.removePrefix("#")
        when (colorString.length) {
            6 -> Color(("FF$colorString").toLong(16))
            8 -> Color(colorString.toLong(16))
            else -> fallback
        }
    } catch (_: Exception) {
        fallback
    }
}
