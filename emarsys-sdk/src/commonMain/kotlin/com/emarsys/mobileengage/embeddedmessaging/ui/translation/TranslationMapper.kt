package com.emarsys.mobileengage.embeddedmessaging.ui.translation

import com.emarsys.mobileengage.embeddedmessaging.EmbeddedMessagingContextApi

class TranslationMapper: TranslationMapperApi {

    override fun map(embeddedMessagingContext: EmbeddedMessagingContextApi): StringResources {
        val translations = embeddedMessagingContext.metaData?.labels
        return translations?.let {
            StringResources(
                allMessagesFilterButtonLabel = it.allMessagesFilterButtonLabel,
                allMessagesFilterButtonAltText = it.allMessagesFilterButtonAltText,
                unreadMessagesFilterButtonLabel = it.unreadMessagesFilterButtonLabel,
                unreadMessagesFilterButtonAltText = it.unreadMessagesFilterButtonAltText,
                categoriesFilterButtonLabel = it.categoriesFilterButtonLabel,
                categoriesFilterIconAltText = it.categoriesFilterIconAltText,
                categoriesFilterDialogTitle = it.categoriesFilterDialogTitle,
                categoriesFilterDialogSubtitle = it.categoriesFilterDialogSubtitle,
                categoryFilterChipIconAltText = it.categoryFilterChipIconAltText,
                categoriesFilterDialogResetButtonLabel = it.categoriesFilterDialogResetButtonLabel,
                categoriesFilterDialogResetButtonAltText = it.categoriesFilterDialogResetButtonAltText,
                categoriesFilterDialogApplyButtonLabel = it.categoriesFilterDialogApplyButtonLabel,
                categoriesFilterDialogCloseButtonAltText = it.categoriesFilterDialogCloseButtonAltText,
                pinnedMessagesTitle = it.pinnedMessagesTitle,
                detailedMessageCloseButtonLabel = it.detailedMessageCloseButtonLabel,
                detailedMessageCloseButtonAltText = it.detailedMessageCloseButtonAltText,
                detailedMessageDeleteButtonLabel = it.detailedMessageDeleteButtonLabel,
                detailedMessageDeleteButtonAltText = it.detailedMessageDeleteButtonAltText,
                emptyStateTitle = it.emptyStateTitle,
                emptyStateDescription = it.emptyStateDescription,
                deleteMessageDialogTitle = it.deleteMessageDialogTitle,
                deleteMessageDialogDescription = it.deleteMessageDialogDescription,
                deleteMessageDialogCancelButtonLabel = it.deleteMessageDialogCancelButtonLabel,
                deleteMessageDialogCancelButtonAltText = it.deleteMessageDialogCancelButtonAltText,
                deleteMessageDialogConfirmButtonLabel = it.deleteMessageDialogConfirmButtonLabel,
                deleteMessageDialogConfirmButtonAltText = it.deleteMessageDialogConfirmButtonAltText,
            )
        } ?: StringResources()
    }
}