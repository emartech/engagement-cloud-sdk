package com.emarsys.mobileengage.embeddedmessaging.ui.theme

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
import com.emarsys.mobileengage.embeddedmessaging.EmbeddedMessagingContextApi
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.Dimensions.DEFAULT_ELEVATION
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.Dimensions.ZERO_ELEVATION
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.Shapes.ZERO_CORNER_RADIUS

internal class ThemeMapper(private val embeddedMessagingContext: EmbeddedMessagingContextApi) {

    @Composable
    fun mapColorScheme(): ColorScheme {
        val colors = embeddedMessagingContext.metaData?.design?.fillColor
        return colors?.let {
            ColorScheme(
                primary = colors.primary.toColor(MaterialTheme.colorScheme.primary),
                onPrimary = colors.onPrimary.toColor(MaterialTheme.colorScheme.onPrimary),
                primaryContainer = colors.primaryContainer.toColor(MaterialTheme.colorScheme.primaryContainer),
                onPrimaryContainer = colors.onPrimaryContainer.toColor(MaterialTheme.colorScheme.onPrimaryContainer),
                secondary = colors.secondary.toColor(MaterialTheme.colorScheme.secondary),
                onSecondary = colors.onSecondary.toColor(MaterialTheme.colorScheme.onSecondary),
                secondaryContainer = colors.secondaryContainer.toColor(MaterialTheme.colorScheme.secondaryContainer),
                onSecondaryContainer = colors.onSecondaryContainer.toColor(MaterialTheme.colorScheme.onSecondaryContainer),
                tertiary = colors.tertiary.toColor(MaterialTheme.colorScheme.tertiary),
                onTertiary = colors.onTertiary.toColor(MaterialTheme.colorScheme.onTertiary),
                tertiaryContainer = colors.tertiaryContainer.toColor(MaterialTheme.colorScheme.tertiaryContainer),
                onTertiaryContainer = colors.onTertiaryContainer.toColor(MaterialTheme.colorScheme.onTertiaryContainer),
                error = colors.error.toColor(MaterialTheme.colorScheme.error),
                onError = colors.onError.toColor(MaterialTheme.colorScheme.onError),
                errorContainer = colors.errorContainer.toColor(MaterialTheme.colorScheme.errorContainer),
                onErrorContainer = colors.onErrorContainer.toColor(MaterialTheme.colorScheme.onErrorContainer),
                background = colors.background.toColor(MaterialTheme.colorScheme.background),
                onBackground = colors.onBackground.toColor(MaterialTheme.colorScheme.onBackground),
                surface = colors.surface.toColor(MaterialTheme.colorScheme.surface),
                onSurface = colors.onSurface.toColor(MaterialTheme.colorScheme.onSurface),
                surfaceVariant = colors.surfaceVariant.toColor(MaterialTheme.colorScheme.surfaceVariant),
                onSurfaceVariant = colors.onSurfaceVariant.toColor(MaterialTheme.colorScheme.onSurfaceVariant),
                surfaceContainer = colors.surfaceContainer.toColor(MaterialTheme.colorScheme.surfaceContainer),
                surfaceContainerHigh = colors.surfaceContainerHigh.toColor(MaterialTheme.colorScheme.surfaceContainerHigh),
                surfaceContainerHighest = colors.surfaceContainerHighest.toColor(MaterialTheme.colorScheme.surfaceContainerHighest),
                surfaceContainerLow = colors.surfaceContainerLow.toColor(MaterialTheme.colorScheme.surfaceContainerLow),
                surfaceContainerLowest = colors.surfaceContainerLowest.toColor(MaterialTheme.colorScheme.surfaceContainerLowest),
                surfaceDim = colors.surfaceDim.toColor(MaterialTheme.colorScheme.surfaceDim),
                surfaceBright = colors.surfaceBright.toColor(MaterialTheme.colorScheme.surfaceBright),
                outline = colors.outline.toColor(MaterialTheme.colorScheme.outline),
                outlineVariant = colors.outlineVariant.toColor(MaterialTheme.colorScheme.outlineVariant),
                inverseSurface = colors.inverseSurface.toColor(MaterialTheme.colorScheme.inverseSurface),
                inverseOnSurface = colors.inverseOnSurface.toColor(MaterialTheme.colorScheme.inverseOnSurface),
                inversePrimary = colors.inversePrimary.toColor(MaterialTheme.colorScheme.inversePrimary),
                scrim = colors.scrim.toColor(MaterialTheme.colorScheme.scrim),

                surfaceTint = MaterialTheme.colorScheme.surfaceTint,
                primaryFixed = MaterialTheme.colorScheme.primaryFixed,
                primaryFixedDim = MaterialTheme.colorScheme.primaryFixedDim,
                onPrimaryFixed = MaterialTheme.colorScheme.onPrimaryFixed,
                onPrimaryFixedVariant = MaterialTheme.colorScheme.onPrimaryFixedVariant,
                secondaryFixed = MaterialTheme.colorScheme.secondaryFixed,
                secondaryFixedDim = MaterialTheme.colorScheme.secondaryFixedDim,
                onSecondaryFixed = MaterialTheme.colorScheme.onSecondaryFixed,
                onSecondaryFixedVariant = MaterialTheme.colorScheme.onSecondaryFixedVariant,
                tertiaryFixed = MaterialTheme.colorScheme.tertiaryFixed,
                tertiaryFixedDim = MaterialTheme.colorScheme.tertiaryFixedDim,
                onTertiaryFixed = MaterialTheme.colorScheme.onTertiaryFixed,
                onTertiaryFixedVariant = MaterialTheme.colorScheme.onTertiaryFixedVariant
            )
        } ?: MaterialTheme.colorScheme
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

    fun mapMisc() : EmbeddedMessagingDesignValues {
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

    private fun String.toColor(fallback: Color): Color {
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

    private fun Double.toTextStyle(): TextStyle {
        return TextStyle(fontSize = this.sp)
    }
}