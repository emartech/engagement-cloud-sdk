package com.sap.ec.mobileengage.embeddedmessaging.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.sap.ec.mobileengage.embeddedmessaging.ui.theme.toColor

object EmbeddedMessagingUiConstants {

    internal object Dimensions {
        val MESSAGE_ITEM_IMAGE_SIZE = 54.dp

        val FLOATING_ACTION_BUTTON_SIZE = 40.dp

        val DEFAULT_ELEVATION = 8.dp
        val ZERO_ELEVATION = 0.dp

        val ZERO_PADDING = 0.dp
        val SMALL_PADDING = 4.dp
        val DEFAULT_PADDING = 8.dp
        val LARGE_PADDING = 16.dp

        val DIALOG_CONTAINER_PADDING = 16.dp

        val DEFAULT_SPACING = 8.dp
        val ZERO_SPACING = 0.dp
    }

    internal object Shapes {
        val ZERO_CORNER_RADIUS = 0.dp
    }

    internal object Colors {
        @Composable
        fun getDefaultColorScheme(): ColorScheme = ColorScheme(
            primary = EmbeddedMessagingConstants.ColorDefaults.PRIMARY.toColor(MaterialTheme.colorScheme.primary),
            onPrimary = EmbeddedMessagingConstants.ColorDefaults.ON_PRIMARY.toColor(MaterialTheme.colorScheme.onPrimary),
            primaryContainer = EmbeddedMessagingConstants.ColorDefaults.PRIMARY_CONTAINER.toColor(MaterialTheme.colorScheme.primaryContainer),
            onPrimaryContainer = EmbeddedMessagingConstants.ColorDefaults.ON_PRIMARY_CONTAINER.toColor(MaterialTheme.colorScheme.onPrimaryContainer),
            secondary = EmbeddedMessagingConstants.ColorDefaults.SECONDARY.toColor(MaterialTheme.colorScheme.secondary),
            onSecondary = EmbeddedMessagingConstants.ColorDefaults.ON_SECONDARY.toColor(MaterialTheme.colorScheme.onSecondary),
            secondaryContainer = EmbeddedMessagingConstants.ColorDefaults.SECONDARY_CONTAINER.toColor(MaterialTheme.colorScheme.secondaryContainer),
            onSecondaryContainer = EmbeddedMessagingConstants.ColorDefaults.ON_SECONDARY_CONTAINER.toColor(MaterialTheme.colorScheme.onSecondaryContainer),
            tertiary = EmbeddedMessagingConstants.ColorDefaults.TERTIARY.toColor(MaterialTheme.colorScheme.tertiary),
            onTertiary = EmbeddedMessagingConstants.ColorDefaults.ON_TERTIARY.toColor(MaterialTheme.colorScheme.onTertiary),
            tertiaryContainer = EmbeddedMessagingConstants.ColorDefaults.TERTIARY_CONTAINER.toColor(MaterialTheme.colorScheme.tertiaryContainer),
            onTertiaryContainer = EmbeddedMessagingConstants.ColorDefaults.ON_TERTIARY_CONTAINER.toColor(MaterialTheme.colorScheme.onTertiaryContainer),
            error = EmbeddedMessagingConstants.ColorDefaults.ERROR.toColor(MaterialTheme.colorScheme.error),
            onError = EmbeddedMessagingConstants.ColorDefaults.ON_ERROR.toColor(MaterialTheme.colorScheme.onError),
            errorContainer = EmbeddedMessagingConstants.ColorDefaults.ERROR_CONTAINER.toColor(MaterialTheme.colorScheme.errorContainer),
            onErrorContainer = EmbeddedMessagingConstants.ColorDefaults.ON_ERROR_CONTAINER.toColor(MaterialTheme.colorScheme.onErrorContainer),
            background = EmbeddedMessagingConstants.ColorDefaults.BACKGROUND.toColor(MaterialTheme.colorScheme.background),
            onBackground = EmbeddedMessagingConstants.ColorDefaults.ON_BACKGROUND.toColor(MaterialTheme.colorScheme.onBackground),
            surface = EmbeddedMessagingConstants.ColorDefaults.SURFACE.toColor(MaterialTheme.colorScheme.surface),
            onSurface = EmbeddedMessagingConstants.ColorDefaults.ON_SURFACE.toColor(MaterialTheme.colorScheme.onSurface),
            surfaceVariant = EmbeddedMessagingConstants.ColorDefaults.SURFACE_VARIANT.toColor(MaterialTheme.colorScheme.surfaceVariant),
            onSurfaceVariant = EmbeddedMessagingConstants.ColorDefaults.ON_SURFACE_VARIANT.toColor(MaterialTheme.colorScheme.onSurfaceVariant),
            surfaceContainer = EmbeddedMessagingConstants.ColorDefaults.SURFACE_CONTAINER.toColor(MaterialTheme.colorScheme.surfaceContainer),
            surfaceContainerHigh = EmbeddedMessagingConstants.ColorDefaults.SURFACE_CONTAINER_HIGH.toColor(MaterialTheme.colorScheme.surfaceContainerHigh),
            surfaceContainerHighest = EmbeddedMessagingConstants.ColorDefaults.SURFACE_CONTAINER_HIGHEST.toColor(MaterialTheme.colorScheme.surfaceContainerHighest),
            surfaceContainerLow = EmbeddedMessagingConstants.ColorDefaults.SURFACE_CONTAINER_LOW.toColor(MaterialTheme.colorScheme.surfaceContainerLow),
            surfaceContainerLowest = EmbeddedMessagingConstants.ColorDefaults.SURFACE_CONTAINER_LOWEST.toColor(MaterialTheme.colorScheme.surfaceContainerLowest),
            surfaceDim = EmbeddedMessagingConstants.ColorDefaults.SURFACE_DIM.toColor(MaterialTheme.colorScheme.surfaceDim),
            surfaceBright = EmbeddedMessagingConstants.ColorDefaults.SURFACE_BRIGHT.toColor(MaterialTheme.colorScheme.surfaceBright),
            outline = EmbeddedMessagingConstants.ColorDefaults.OUTLINE.toColor(MaterialTheme.colorScheme.outline),
            outlineVariant = EmbeddedMessagingConstants.ColorDefaults.OUTLINE_VARIANT.toColor(MaterialTheme.colorScheme.outlineVariant),
            inverseSurface = EmbeddedMessagingConstants.ColorDefaults.INVERSE_SURFACE.toColor(MaterialTheme.colorScheme.inverseSurface),
            inverseOnSurface = EmbeddedMessagingConstants.ColorDefaults.INVERSE_ON_SURFACE.toColor(MaterialTheme.colorScheme.inverseOnSurface),
            inversePrimary = EmbeddedMessagingConstants.ColorDefaults.INVERSE_PRIMARY.toColor(MaterialTheme.colorScheme.inversePrimary),
            scrim = EmbeddedMessagingConstants.ColorDefaults.SCRIM.toColor(MaterialTheme.colorScheme.scrim),
            surfaceTint = EmbeddedMessagingConstants.ColorDefaults.SURFACE_TINT.toColor(MaterialTheme.colorScheme.surfaceTint),
            primaryFixed = EmbeddedMessagingConstants.ColorDefaults.PRIMARY_FIXED.toColor(MaterialTheme.colorScheme.primaryFixed),
            primaryFixedDim = EmbeddedMessagingConstants.ColorDefaults.PRIMARY_FIXED_DIM.toColor(MaterialTheme.colorScheme.primaryFixedDim),
            onPrimaryFixed = EmbeddedMessagingConstants.ColorDefaults.ON_PRIMARY_FIXED.toColor(MaterialTheme.colorScheme.onPrimaryFixed),
            onPrimaryFixedVariant = EmbeddedMessagingConstants.ColorDefaults.ON_PRIMARY_FIXED_VARIANT.toColor(MaterialTheme.colorScheme.onPrimaryFixedVariant),
            secondaryFixed = EmbeddedMessagingConstants.ColorDefaults.SECONDARY_FIXED.toColor(MaterialTheme.colorScheme.secondaryFixed),
            secondaryFixedDim = EmbeddedMessagingConstants.ColorDefaults.SECONDARY_FIXED_DIM.toColor(MaterialTheme.colorScheme.secondaryFixedDim),
            onSecondaryFixed = EmbeddedMessagingConstants.ColorDefaults.ON_SECONDARY_FIXED.toColor(MaterialTheme.colorScheme.onSecondaryFixed),
            onSecondaryFixedVariant = EmbeddedMessagingConstants.ColorDefaults.ON_SECONDARY_FIXED_VARIANT.toColor(MaterialTheme.colorScheme.onSecondaryFixedVariant),
            tertiaryFixed = EmbeddedMessagingConstants.ColorDefaults.TERTIARY_FIXED.toColor(MaterialTheme.colorScheme.tertiaryFixed),
            tertiaryFixedDim = EmbeddedMessagingConstants.ColorDefaults.TERTIARY_FIXED_DIM.toColor(MaterialTheme.colorScheme.tertiaryFixedDim),
            onTertiaryFixed = EmbeddedMessagingConstants.ColorDefaults.ON_TERTIARY_FIXED.toColor(MaterialTheme.colorScheme.onTertiaryFixed),
            onTertiaryFixedVariant = EmbeddedMessagingConstants.ColorDefaults.ON_TERTIARY_FIXED_VARIANT.toColor(MaterialTheme.colorScheme.onTertiaryFixedVariant),
        )
    }
}