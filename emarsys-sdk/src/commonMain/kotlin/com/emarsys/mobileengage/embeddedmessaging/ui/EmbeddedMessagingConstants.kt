package com.emarsys.mobileengage.embeddedmessaging.ui

import androidx.compose.ui.unit.dp

internal object EmbeddedMessagingConstants {
    const val BASE64_PLACEHOLDER_IMAGE = """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"""

    val MESSAGE_ITEM_IMAGE_SIZE = 54.dp

    val DEFAULT_ELEVATION = 8.dp
    val ZERO_ELEVATION = 0.dp

    val DEFAULT_PADDING = 8.dp
    val ZERO_PADDING = 0.dp

    val DIALOG_CONTAINER_PADDING = 16.dp

    val DEFAULT_SPACING = 8.dp
    val ZERO_SPACING = 0.dp

    internal object Translations{
        const val FILTER_ALL_BUTTON_LABEL = "All"
        const val FILTER_ALL_BUTTON_ALT_TEXT = "All messages filter button"
        const val FILTER_UNREAD_BUTTON_LABEL = "Unread"
        const val FILTER_UNREAD_BUTTON_ALT_TEXT = "Unread messages filter button"
        const val CATEGORIES_FILTER_BUTTON_LABEL = "Categories"
        const val CATEGORIES_FILTER_BUTTON_ICON_ALT_TEXT = "Category filter button"
        const val CATEGORIES_FILTER_DIALOG_TITLE = "Categories"
        const val CATEGORIES_FILTER_DIALOG_SUBTITLE = "Select Category Filters"
        const val CATEGORY_FILTER_CHIP_ICON_ALT_TEXT = "selected"
        const val CATEGORIES_FILTER_DIALOG_RESET_BUTTON_LABEL = "Reset"
        const val CATEGORIES_FILTER_DIALOG_RESET_BUTTON_ALT_TEXT = "Reset Selected Categories Button"
        const val CATEGORIES_FILTER_DIALOG_APPLY_BUTTON_LABEL = "Apply"
        const val CATEGORIES_FILTER_DIALOG_CLOSE_BUTTON_ALT_TEXT = "Close categories dialog"
        const val PINNED_MESSAGES_TITLE = "Pinned Messages"
        const val DETAILED_MESSAGE_CLOSE_BUTTON_LABEL = "Close"
        const val DETAILED_MESSAGE_CLOSE_BUTTON_ALT_TEXT = "Detailed message close button"
        const val DETAILED_MESSAGE_DELETE_BUTTON_LABEL = "Delete Message"
        const val DETAILED_MESSAGE_DELETE_BUTTON_ALT_TEXT = "Delete message button"
        const val EMPTY_STATE_TITLE = "No Messages"
        const val EMPTY_STATE_DESCRIPTION = "You have no messages in the selected view."
        const val DELETE_MESSAGE_DIALOG_TITLE = "Delete Message?"
        const val DELETE_MESSAGE_DIALOG_DESCRIPTION = "By deleting this, it will be no longer available"
        const val DELETE_MESSAGE_DIALOG_CANCEL_BUTTON_LABEL = "Cancel"
        const val DELETE_MESSAGE_DIALOG_CANCEL_BUTTON_ALT_TEXT = "Delete message cancel button"
        const val DELETE_MESSAGE_DIALOG_CONFIRM_BUTTON_LABEL = "Delete"
        const val DELETE_MESSAGE_DIALOG_CONFIRM_BUTTON_ALT_TEXT = "Confirm delete message button"
    }
}