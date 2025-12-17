package com.emarsys.mobileengage.embeddedmessaging.ui.translation

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
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
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.EMPTY_STATE_DESCRIPTION
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.EMPTY_STATE_TITLE
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.ERROR_STATE_NO_INTERNET_STATE_DESCRIPTION
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.ERROR_STATE_NO_INTERNET_STATE_REFRESH_ICON_ALT_TEXT
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.ERROR_STATE_NO_INTERNET_STATE_RETRY_BUTTON_LABEL
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.ERROR_STATE_NO_INTERNET_STATE_TITLE
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.FILTER_ALL_BUTTON_LABEL
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.FILTER_UNREAD_BUTTON_LABEL
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Translations.PINNED_MESSAGES_TITLE

@Immutable
internal data class StringResources(
    val allMessagesFilterButtonLabel: String = FILTER_ALL_BUTTON_LABEL,
    val unreadMessagesFilterButtonLabel: String = FILTER_UNREAD_BUTTON_LABEL,
    val categoriesFilterButtonLabel: String = CATEGORIES_FILTER_BUTTON_LABEL,
    val categoriesFilterIconAltText: String = CATEGORIES_FILTER_BUTTON_ICON_ALT_TEXT,
    val categoriesFilterDialogTitle: String = CATEGORIES_FILTER_DIALOG_TITLE,
    val categoriesFilterDialogSubtitle: String = CATEGORIES_FILTER_DIALOG_SUBTITLE,
    val selectedCategoryFilterChipIconAltText: String = CATEGORY_FILTER_CHIP_ICON_ALT_TEXT,
    val categoriesFilterDialogResetButtonLabel: String = CATEGORIES_FILTER_DIALOG_RESET_BUTTON_LABEL,
    val categoriesFilterDialogApplyButtonLabel: String = CATEGORIES_FILTER_DIALOG_APPLY_BUTTON_LABEL,
    val pinnedMessagesTitle: String = PINNED_MESSAGES_TITLE,
    val detailedMessageEmptyStateText: String = DETAILED_MESSAGE_EMPTY_STATE_TEXT,
    val detailedMessageCloseButtonLabel: String = DETAILED_MESSAGE_CLOSE_BUTTON_LABEL,
    val detailedMessageDeleteButtonLabel: String = DETAILED_MESSAGE_DELETE_BUTTON_LABEL,
    val emptyStateTitle: String = EMPTY_STATE_TITLE,
    val emptyStateDescription: String = EMPTY_STATE_DESCRIPTION,
    val deleteMessageDialogTitle: String = DELETE_MESSAGE_DIALOG_TITLE,
    val deleteMessageDialogDescription: String = DELETE_MESSAGE_DIALOG_DESCRIPTION,
    val deleteMessageDialogCancelButtonLabel: String = DELETE_MESSAGE_DIALOG_CANCEL_BUTTON_LABEL,
    val deleteMessageDialogConfirmButtonLabel: String = DELETE_MESSAGE_DIALOG_CONFIRM_BUTTON_LABEL,
    val deleteIconButtonAltText: String = DELETE_ICON_BUTTON_ALT_TEXT,
    val closeIconButtonAltText: String = CLOSE_ICON_BUTTON_ALT_TEXT,
    val noInternetStateTitle: String = ERROR_STATE_NO_INTERNET_STATE_TITLE,
    val noInternetStateDescription: String = ERROR_STATE_NO_INTERNET_STATE_DESCRIPTION,
    val noInternetStateRetryButtonLabel: String = ERROR_STATE_NO_INTERNET_STATE_RETRY_BUTTON_LABEL,
    val noInternetStateRefreshIconAltText: String = ERROR_STATE_NO_INTERNET_STATE_REFRESH_ICON_ALT_TEXT,
)

internal val LocalStringResources = compositionLocalOf {
    StringResources()
}
