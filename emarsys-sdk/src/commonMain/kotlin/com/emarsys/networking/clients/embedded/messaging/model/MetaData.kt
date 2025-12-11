package com.emarsys.networking.clients.embedded.messaging.model

import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.CATEGORIES_FILTER_BUTTON_ICON_ALT_TEXT
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.CATEGORIES_FILTER_BUTTON_LABEL
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.CATEGORIES_FILTER_DIALOG_APPLY_BUTTON_LABEL
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.CATEGORIES_FILTER_DIALOG_RESET_BUTTON_LABEL
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.CATEGORIES_FILTER_DIALOG_SUBTITLE
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.CATEGORIES_FILTER_DIALOG_TITLE
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.CATEGORY_FILTER_CHIP_ICON_ALT_TEXT
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.CLOSE_ICON_BUTTON_ALT_TEXT
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.DELETE_ICON_BUTTON_ALT_TEXT
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.DELETE_MESSAGE_DIALOG_CANCEL_BUTTON_LABEL
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.DELETE_MESSAGE_DIALOG_CONFIRM_BUTTON_LABEL
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.DELETE_MESSAGE_DIALOG_DESCRIPTION
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.DELETE_MESSAGE_DIALOG_TITLE
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.DETAILED_MESSAGE_CLOSE_BUTTON_LABEL
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.DETAILED_MESSAGE_DELETE_BUTTON_LABEL
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.DETAILED_MESSAGE_EMPTY_STATE_TEXT
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.FILTER_ALL_BUTTON_LABEL
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.FILTER_UNREAD_BUTTON_LABEL
import kotlinx.serialization.Serializable

@Serializable
internal data class MetaData(
    val version: String,
    val labels: Labels,
    val design: DesignMetaData?
)

@Serializable
internal data class Labels(
    // TODO: Remove these after backend team changes the response
    val allMessagesHeader: String,
    val unreadMessagesHeader: String,
    val filterCategories: String,
    val detailedMessageCloseButton: String,
    val deleteDetailedMessageButton: String,

    val pinnedMessagesTitle: String,
    val emptyStateTitle: String,
    val emptyStateDescription: String,
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
    val closeIconButtonAltText: String = CLOSE_ICON_BUTTON_ALT_TEXT
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

    val surfaceTint: String? = null,
    val primaryFixed: String? = null,
    val primaryFixedDim: String? = null,
    val onPrimaryFixed: String? = null,
    val onPrimaryFixedVariant: String? = null,
    val secondaryFixed: String? = null,
    val secondaryFixedDim: String? = null,
    val onSecondaryFixed: String? = null,
    val onSecondaryFixedVariant: String? = null,
    val tertiaryFixed: String? = null,
    val tertiaryFixedDim: String? = null,
    val onTertiaryFixed: String? = null,
    val onTertiaryFixedVariant: String? = null,

    // TODO: Remove these after backend team changes the response
    val selectedState: String,
    val disabledState: String,
    val hoverState: String,
    val pressedState: String,
    val focusState: String,

    val warning: String,
    val onWarning: String,
    val warningContainer: String,
    val onWarningContainer: String,
    val success: String,
    val onSuccess: String,
    val successContainer: String,
    val onSuccessContainer: String,
    val info: String,
    val onInfo: String,
    val infoContainer: String,
    val onInfoContainer: String
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
internal data class MiscV2(
    val messageItemMargin: Double,
    val messageItemElevation: Double,

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

@Serializable
internal data class Misc(
    val dialogCornerRadius: Double,
    val categoryButtonCornerRadius: Double,
    val messageItemCornerRadius: Double,
    val filterButtonCornerRadius: Double,
    val actionButtonCornerRadius: Double,
    val chipCornerRadius: Double,
    val detailViewCornerRadius: Double,
    val headerCornerRadius: Double,
    val footerCornerRadius: Double,
    val modalCornerRadius: Double,
    val snackbarCornerRadius: Double,
    val tooltipCornerRadius: Double,
    val badgeCornerRadius: Double,
    val avatarCornerRadius: Double,
    val imageCornerRadius: Double,

    val messageItemPadding: Double,
    val messageItemMargin: Double,
    val messageItemSpacing: Double,
    val dialogPadding: Double,
    val dialogMargin: Double,
    val dialogSpacing: Double,
    val categoryButtonPadding: Double,
    val categoryButtonMargin: Double,
    val categoryButtonSpacing: Double,
    val filterButtonPadding: Double,
    val filterButtonMargin: Double,
    val actionButtonPadding: Double,
    val actionButtonMargin: Double,
    val headerPadding: Double,
    val headerMargin: Double,
    val headerSpacing: Double,
    val footerPadding: Double,
    val footerMargin: Double,
    val footerSpacing: Double,
    val listPadding: Double,
    val listMargin: Double,
    val listSpacing: Double,
    val detailViewPadding: Double,
    val detailViewMargin: Double,
    val detailViewSpacing: Double,

    val dividerWidth: Double,
    val dividerColor: String,
    val categoryButtonStrokeColor: String,
    val categoryButtonStrokeSize: Double,
    val messageItemStrokeColor: String,
    val messageItemStrokeSize: Double,
    val filterButtonStrokeColor: String,
    val filterButtonStrokeSize: Double,
    val actionButtonStrokeColor: String,
    val actionButtonStrokeSize: Double,

    val messageItemElevation: Double,
    val dialogElevation: Double,
    val categoryButtonElevation: Double,
    val filterButtonElevation: Double,
    val actionButtonElevation: Double,
    val headerElevation: Double,
    val footerElevation: Double,
    val modalElevation: Double,
    val snackbarElevation: Double,
    val tooltipElevation: Double,

    val messageItemIconSize: Double,
    val categoryButtonIconSize: Double,
    val filterButtonIconSize: Double,
    val actionButtonIconSize: Double,
    val headerIconSize: Double,
    val footerIconSize: Double,
    val dialogIconSize: Double,
    val snackbarIconSize: Double,
    val tooltipIconSize: Double,
    val listContentPadding: Double,
    val listItemSpacing: Double,
    val listItemMargin: Double,
    val tabButtonPadding: Double,
    val tabButtonSpacing: Double,
    val filterButtonSpacing: Double,
    val actionButtonSpacing: Double,
    val compactOverlayWidth: Double,
    val compactOverlayMaxHeight: Double,
    val compactOverlayPadding: Double,
    val compactOverlaySpacing: Double,
    val compactOverlayCornerRadius: Double,
    val compactOverlayElevation: Double,
    val emptyStatePadding: Double,
    val emptyStateSpacing: Double,
    val emptyStateIconPadding: Double,
    val detailViewImageHeight: Double,
    val detailViewImageCornerRadius: Double,
    val detailViewTagPadding: Double,
    val detailViewTagSpacing: Double,
    val detailViewActionSpacing: Double,
    val swipeDeleteBackgroundCornerRadius: Double,
    val swipeDeleteButtonSize: Double,
    val swipeDeleteButtonCornerRadius: Double,
    val swipeDeleteIconSize: Double
)