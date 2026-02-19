package com.sap.ec.networking.clients.embedded.messaging.model

import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.CATEGORIES_FILTER_BUTTON_ICON_ALT_TEXT
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.CATEGORIES_FILTER_BUTTON_LABEL
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.CATEGORIES_FILTER_DIALOG_APPLY_BUTTON_LABEL
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.CATEGORIES_FILTER_DIALOG_RESET_BUTTON_LABEL
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.CATEGORIES_FILTER_DIALOG_SUBTITLE
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.CATEGORIES_FILTER_DIALOG_TITLE
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.CATEGORY_FILTER_CHIP_ICON_ALT_TEXT
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.CLOSE_ICON_BUTTON_ALT_TEXT
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.DELETE_ICON_BUTTON_ALT_TEXT
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.DELETE_MESSAGE_DIALOG_CANCEL_BUTTON_LABEL
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.DELETE_MESSAGE_DIALOG_CONFIRM_BUTTON_LABEL
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.DELETE_MESSAGE_DIALOG_DESCRIPTION
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.DELETE_MESSAGE_DIALOG_TITLE
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.DETAILED_MESSAGE_CLOSE_BUTTON_LABEL
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.DETAILED_MESSAGE_DELETE_BUTTON_LABEL
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.DETAILED_MESSAGE_EMPTY_STATE_TEXT
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.EMPTY_STATE_FILTERED_CLEAR_FILTERS_BUTTON_LABEL
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.EMPTY_STATE_FILTERED_CLEAR_FILTERS_ICON_ALT_TEXT
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.EMPTY_STATE_FILTERED_DESCRIPTION
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.EMPTY_STATE_FILTERED_TITLE
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.ERROR_STATE_NO_CONNECTION_DESCRIPTION
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.ERROR_STATE_NO_CONNECTION_REFRESH_ICON_ALT_TEXT
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.ERROR_STATE_NO_CONNECTION_RETRY_BUTTON_LABEL
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.ERROR_STATE_NO_CONNECTION_TITLE
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.FAILED_TO_DELETE_MESSAGE_TEXT
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.FAILED_TO_LOAD_MORE_MESSAGES_TEXT
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.FILTER_ALL_BUTTON_LABEL
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.FILTER_UNREAD_BUTTON_LABEL
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.REFRESH_ERROR_MESSAGE_TEXT
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.SNACKBAR_CONNECTION_RESTORED
import kotlinx.serialization.Serializable

@Serializable
internal data class MetaData(
    val version: String,
    val labels: Labels,
    val design: DesignMetaData?
)

@Serializable
internal data class Labels(
    val pinnedMessagesTitle: String,
    val emptyStateTitle: String,
    val emptyStateDescription: String,
    val emptyStateFilteredTitle: String = EMPTY_STATE_FILTERED_TITLE,
    val emptyStateFilteredDescription: String = EMPTY_STATE_FILTERED_DESCRIPTION,
    val emptyStateFilteredClearFiltersButtonLabel: String = EMPTY_STATE_FILTERED_CLEAR_FILTERS_BUTTON_LABEL,
    val emptyStateFilteredClearFiltersIconAltText: String = EMPTY_STATE_FILTERED_CLEAR_FILTERS_ICON_ALT_TEXT,
    val allMessagesFilterButtonLabel: String = FILTER_ALL_BUTTON_LABEL,
    val unreadMessagesFilterButtonLabel: String = FILTER_UNREAD_BUTTON_LABEL,
    val categoriesFilterButtonLabel: String = CATEGORIES_FILTER_BUTTON_LABEL,
    val categoriesFilterIconAltText: String = CATEGORIES_FILTER_BUTTON_ICON_ALT_TEXT,
    val categoriesFilterDialogTitle: String = CATEGORIES_FILTER_DIALOG_TITLE,
    val categoriesFilterDialogSubtitle: String = CATEGORIES_FILTER_DIALOG_SUBTITLE,
    val selectedCategoryFilterChipIconAltText: String = CATEGORY_FILTER_CHIP_ICON_ALT_TEXT,
    val categoriesFilterDialogResetButtonLabel: String = CATEGORIES_FILTER_DIALOG_RESET_BUTTON_LABEL,
    val categoriesFilterDialogApplyButtonLabel: String = CATEGORIES_FILTER_DIALOG_APPLY_BUTTON_LABEL,
    val detailedMessageEmptyStateText: String = DETAILED_MESSAGE_EMPTY_STATE_TEXT,
    val detailedMessageCloseButtonLabel: String = DETAILED_MESSAGE_CLOSE_BUTTON_LABEL,
    val detailedMessageDeleteButtonLabel: String = DETAILED_MESSAGE_DELETE_BUTTON_LABEL,
    val deleteMessageDialogTitle: String = DELETE_MESSAGE_DIALOG_TITLE,
    val deleteMessageDialogDescription: String = DELETE_MESSAGE_DIALOG_DESCRIPTION,
    val deleteMessageDialogCancelButtonLabel: String = DELETE_MESSAGE_DIALOG_CANCEL_BUTTON_LABEL,
    val deleteMessageDialogConfirmButtonLabel: String = DELETE_MESSAGE_DIALOG_CONFIRM_BUTTON_LABEL,
    val deleteIconButtonAltText: String = DELETE_ICON_BUTTON_ALT_TEXT,
    val closeIconButtonAltText: String = CLOSE_ICON_BUTTON_ALT_TEXT,
    val errorStateNoConnectionTitle: String = ERROR_STATE_NO_CONNECTION_TITLE,
    val errorStateNoConnectionDescription: String = ERROR_STATE_NO_CONNECTION_DESCRIPTION,
    val snackbarConnectionRestored: String = SNACKBAR_CONNECTION_RESTORED,
    val errorStateNoConnectionRetryButtonLabel: String = ERROR_STATE_NO_CONNECTION_RETRY_BUTTON_LABEL,
    val errorStateNoConnectionRefreshIconAltText: String = ERROR_STATE_NO_CONNECTION_REFRESH_ICON_ALT_TEXT,
    val refreshErrorMessageText: String = REFRESH_ERROR_MESSAGE_TEXT,
    val failedToLoadMoreMessagesText: String = FAILED_TO_LOAD_MORE_MESSAGES_TEXT,
    val failedToDeleteMessageText: String = FAILED_TO_DELETE_MESSAGE_TEXT
)

@Serializable
internal data class DesignMetaData(
    val fillColor: FillColors?,
    val text: TextMetaData?,
    val shapes: ShapesData? = null,
    val misc: Misc?,
)

@Serializable
internal data class FillColors(
    val primary: String,
    val onPrimary: String,
    val primaryContainer: String,
    val onPrimaryContainer: String,
    val secondary: String,
    val onSecondary: String,
    val secondaryContainer: String,
    val onSecondaryContainer: String,
    val tertiary: String,
    val onTertiary: String,
    val tertiaryContainer: String,
    val onTertiaryContainer: String,
    val error: String,
    val onError: String,
    val errorContainer: String,
    val onErrorContainer: String,
    val background: String,
    val onBackground: String,
    val surface: String,
    val onSurface: String,
    val surfaceVariant: String,
    val onSurfaceVariant: String,
    val surfaceContainer: String,
    val surfaceContainerHigh: String,
    val surfaceContainerHighest: String,
    val surfaceContainerLow: String,
    val surfaceContainerLowest: String,
    val surfaceDim: String,
    val surfaceBright: String,
    val outline: String,
    val outlineVariant: String,
    val inverseSurface: String,
    val inverseOnSurface: String,
    val inversePrimary: String,
    val scrim: String,
    val surfaceTint: String,
    val primaryFixed: String,
    val primaryFixedDim: String,
    val onPrimaryFixed: String,
    val onPrimaryFixedVariant: String,
    val secondaryFixed: String,
    val secondaryFixedDim: String,
    val onSecondaryFixed: String,
    val onSecondaryFixedVariant: String,
    val tertiaryFixed: String,
    val tertiaryFixedDim: String,
    val onTertiaryFixed: String,
    val onTertiaryFixedVariant: String,
)

@Serializable
internal data class TextMetaData(
    val displayLargeFontSize: Double,
    val displayMediumFontSize: Double,
    val displaySmallFontSize: Double,
    val headlineLargeFontSize: Double,
    val headlineMediumFontSize: Double,
    val headlineSmallFontSize: Double,
    val titleLargeFontSize: Double,
    val titleMediumFontSize: Double,
    val titleSmallFontSize: Double,
    val bodyLargeFontSize: Double,
    val bodyMediumFontSize: Double,
    val bodySmallFontSize: Double,
    val labelLargeFontSize: Double,
    val labelMediumFontSize: Double,
    val labelSmallFontSize: Double,
)

@Serializable
internal data class ShapesData(
    val extraSmall: ShapeCornerRadius,
    val small: ShapeCornerRadius,
    val medium: ShapeCornerRadius,
    val large: ShapeCornerRadius,
    val extraLarge: ShapeCornerRadius
)

@Serializable
internal data class Misc(
    val messageItemMargin: Double,
    val messageItemElevation: Double,
    val buttonElevation: Double = 0.0,
    val listContentPadding: Double,
    val listItemSpacing: Double,
    val compactOverlayWidth: Double,
    val compactOverlayMaxHeight: Double,
    val compactOverlayCornerRadius: Double,
    val compactOverlayElevation: Double,
    val emptyStateImageUrl: String? = null,
    val messageItemCardCornerRadius: Double? = 0.0,
    val messageItemCardElevation: Double? = 0.0,
    val messageItemImageHeight: Double,
    val messageItemImageClipShape: String, // e.g., "Rectangle", "Circle", "Rounded", "Custom"
    val messageItemImageCornerRadius: Double,
    val messageItemCustomShape: ShapeCornerRadius? = null
)

@Serializable
internal data class ShapeCornerRadius(
    val topStart: Double,
    val topEnd: Double,
    val bottomStart: Double,
    val bottomEnd: Double
)
