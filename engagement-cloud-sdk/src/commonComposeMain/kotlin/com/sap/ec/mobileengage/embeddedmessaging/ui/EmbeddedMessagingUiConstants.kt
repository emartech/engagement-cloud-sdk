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
            primary = "#526525".toColor(MaterialTheme.colorScheme.primary),
            onPrimary = "#ffffff".toColor(MaterialTheme.colorScheme.onPrimary),
            primaryContainer = "#9cbd4c".toColor(MaterialTheme.colorScheme.primaryContainer),
            onPrimaryContainer = "#000000".toColor(MaterialTheme.colorScheme.onPrimaryContainer),
            secondary = "#365e2c".toColor(MaterialTheme.colorScheme.secondary),
            onSecondary = "#ffffff".toColor(MaterialTheme.colorScheme.onSecondary),
            secondaryContainer = "#6ab158".toColor(MaterialTheme.colorScheme.secondaryContainer),
            onSecondaryContainer = "#000000".toColor(MaterialTheme.colorScheme.onSecondaryContainer),
            tertiary = "#6f6334".toColor(MaterialTheme.colorScheme.tertiary),
            onTertiary = "#ffffff".toColor(MaterialTheme.colorScheme.onTertiary),
            tertiaryContainer = "#b19f58".toColor(MaterialTheme.colorScheme.tertiaryContainer),
            onTertiaryContainer = "#000000".toColor(MaterialTheme.colorScheme.onTertiaryContainer),
            error = "#BA1A1A".toColor(MaterialTheme.colorScheme.error),
            onError = "#FFFFFF".toColor(MaterialTheme.colorScheme.onError),
            errorContainer = "#FFDAD6".toColor(MaterialTheme.colorScheme.errorContainer),
            onErrorContainer = "#410002".toColor(MaterialTheme.colorScheme.onErrorContainer),
            background = "#D4D4C0".toColor(MaterialTheme.colorScheme.background),
            onBackground = "#1B1C18".toColor(MaterialTheme.colorScheme.onBackground),
            surface = "#E9E9DD".toColor(MaterialTheme.colorScheme.surface),
            onSurface = "#1B1C18".toColor(MaterialTheme.colorScheme.onSurface),
            surfaceVariant = "#ffffff".toColor(MaterialTheme.colorScheme.surfaceVariant),
            onSurfaceVariant = "#9dae75".toColor(MaterialTheme.colorScheme.onSurfaceVariant),
            surfaceContainer = "#F0EFE8".toColor(MaterialTheme.colorScheme.surfaceContainer),
            surfaceContainerHigh = "#EAE9E2".toColor(MaterialTheme.colorScheme.surfaceContainerHigh),
            surfaceContainerHighest = "#E4E3DD".toColor(MaterialTheme.colorScheme.surfaceContainerHighest),
            surfaceContainerLow = "#F6F5EE".toColor(MaterialTheme.colorScheme.surfaceContainerLow),
            surfaceContainerLowest = "#FFFFFF".toColor(MaterialTheme.colorScheme.surfaceContainerLowest),
            surfaceDim = "#DDD9D1".toColor(MaterialTheme.colorScheme.surfaceDim),
            surfaceBright = "#FDFCF6".toColor(MaterialTheme.colorScheme.surfaceBright),
            outline = "#c8cebb".toColor(MaterialTheme.colorScheme.outline),
            outlineVariant = "#ffffff".toColor(MaterialTheme.colorScheme.outlineVariant),
            inverseSurface = "#30312C".toColor(MaterialTheme.colorScheme.inverseSurface),
            inverseOnSurface = "#F2F1E9".toColor(MaterialTheme.colorScheme.inverseOnSurface),
            inversePrimary = "#f9fbf4".toColor(MaterialTheme.colorScheme.inversePrimary),
            scrim = "#000000".toColor(MaterialTheme.colorScheme.scrim),
            surfaceTint = "#526525".toColor(MaterialTheme.colorScheme.surfaceTint),
            primaryFixed = "#ffffff".toColor(MaterialTheme.colorScheme.primaryFixed),
            primaryFixedDim = "#e4edcf".toColor(MaterialTheme.colorScheme.primaryFixedDim),
            onPrimaryFixed = "#000000".toColor(MaterialTheme.colorScheme.onPrimaryFixed),
            onPrimaryFixedVariant = "#151a0a".toColor(MaterialTheme.colorScheme.onPrimaryFixedVariant),
            secondaryFixed = "#ffffff".toColor(MaterialTheme.colorScheme.secondaryFixed),
            secondaryFixedDim = "#d7ead2".toColor(MaterialTheme.colorScheme.secondaryFixedDim),
            onSecondaryFixed = "#000000".toColor(MaterialTheme.colorScheme.onSecondaryFixed),
            onSecondaryFixedVariant = "#0e190b".toColor(MaterialTheme.colorScheme.onSecondaryFixedVariant),
            tertiaryFixed = "#ffffff".toColor(MaterialTheme.colorScheme.tertiaryFixed),
            tertiaryFixedDim = "#eae5d2".toColor(MaterialTheme.colorScheme.tertiaryFixedDim),
            onTertiaryFixed = "#000000".toColor(MaterialTheme.colorScheme.onTertiaryFixed),
            onTertiaryFixedVariant = "#19160b".toColor(MaterialTheme.colorScheme.onTertiaryFixedVariant),
        )
    }
}