package com.emarsys.mobileengage.embeddedmessaging.ui.translation

import com.emarsys.mobileengage.embeddedmessaging.EmbeddedMessagingContextApi

internal class TranslationMapper : TranslationMapperApi {

    override fun map(embeddedMessagingContext: EmbeddedMessagingContextApi): StringResources {
        val translations = embeddedMessagingContext.metaData?.labels
        return translations?.let {
            StringResources(
                allMessagesFilterButtonLabel = it.allMessagesFilterButtonLabel,
                unreadMessagesFilterButtonLabel = it.unreadMessagesFilterButtonLabel,
                categoriesFilterButtonLabel = it.categoriesFilterButtonLabel,
                categoriesFilterIconAltText = it.categoriesFilterIconAltText,
                categoriesFilterDialogTitle = it.categoriesFilterDialogTitle,
                categoriesFilterDialogSubtitle = it.categoriesFilterDialogSubtitle,
                selectedCategoryFilterChipIconAltText = it.selectedCategoryFilterChipIconAltText,
                categoriesFilterDialogResetButtonLabel = it.categoriesFilterDialogResetButtonLabel,
                categoriesFilterDialogApplyButtonLabel = it.categoriesFilterDialogApplyButtonLabel,
                pinnedMessagesTitle = it.pinnedMessagesTitle,
                detailedMessageEmptyStateText = it.detailedMessageEmptyStateText,
                detailedMessageCloseButtonLabel = it.detailedMessageCloseButtonLabel,
                detailedMessageDeleteButtonLabel = it.detailedMessageDeleteButtonLabel,
                emptyStateTitle = it.emptyStateTitle,
                emptyStateDescription = it.emptyStateDescription,
                emptyStateFilteredTitle = it.emptyStateFilteredTitle,
                emptyStateFilteredDescription = it.emptyStateFilteredDescription,
                emptyStateFilteredClearFiltersButtonLabel = it.emptyStateFilteredClearFiltersButtonLabel,
                emptyStateFilteredClearFiltersIconAltText = it.emptyStateFilteredClearFiltersIconAltText,
                snackbarConnectionRestored = it.snackbarConnectionRestored,
                deleteMessageDialogTitle = it.deleteMessageDialogTitle,
                deleteMessageDialogDescription = it.deleteMessageDialogDescription,
                deleteMessageDialogCancelButtonLabel = it.deleteMessageDialogCancelButtonLabel,
                deleteMessageDialogConfirmButtonLabel = it.deleteMessageDialogConfirmButtonLabel,
                deleteIconButtonAltText = it.deleteIconButtonAltText,
                closeIconButtonAltText = it.closeIconButtonAltText,
                errorStateNoConnectionTitle = it.errorStateNoConnectionTitle,
                errorStateNoConnectionDescription = it.errorStateNoConnectionDescription,
                errorStateNoConnectionRetryButtonLabel = it.errorStateNoConnectionRetryButtonLabel,
                errorStateNoConnectionRefreshIconAltText = it.errorStateNoConnectionRefreshIconAltText,
                refreshErrorMessageText = it.refreshErrorMessageText,
                failedToLoadMoreMessagesText = it.failedToLoadMoreMessagesText
            )
        } ?: StringResources()
    }
}