package com.emarsys.mobileengage.embeddedmessaging.ui.translation

import com.emarsys.mobileengage.embeddedmessaging.EmbeddedMessagingContext
import com.emarsys.networking.clients.embedded.messaging.model.Labels
import com.emarsys.networking.clients.embedded.messaging.model.MetaData
import io.kotest.matchers.shouldBe
import kotlin.test.BeforeTest
import kotlin.test.Test

class TranslationMapperTests {
    private lateinit var translationMapper: TranslationMapper

    @BeforeTest
    fun setUp() {
        translationMapper = TranslationMapper()
    }

    @Test
    fun mapTranslations_should_mapTranslationsFromEmbeddedMessagingContext_when_embeddedMessagingContextIsAvailable() {
        val testEmbeddedMessagingContext = EmbeddedMessagingContext(
            metaData = MetaData(
                "testVersion",
                createTestLabels(),
                design = null
            )
        )

        val expectedStringResources = StringResources(
            allMessagesFilterButtonLabel = "test allMessagesFilterButtonLabel",
            unreadMessagesFilterButtonLabel = "test unreadMessagesFilterButtonLabel",
            categoriesFilterButtonLabel = "test categoriesFilterButtonLabel",
            categoriesFilterIconAltText = "test categoriesFilterIconAltText",
            categoriesFilterDialogTitle = "test categoriesFilterDialogTitle",
            categoriesFilterDialogSubtitle = "test categoriesFilterDialogSubtitle",
            selectedCategoryFilterChipIconAltText = "test categoryFilterChipIconAltText",
            categoriesFilterDialogResetButtonLabel = "test categoriesFilterDialogResetButtonLabel",
            categoriesFilterDialogApplyButtonLabel = "test categoriesFilterDialogApplyButtonLabel",
            pinnedMessagesTitle = "test pinnedMessagesTitle",
            detailedMessageEmptyStateText = "test detailedMessageEmptyStateText",
            detailedMessageCloseButtonLabel = "test detailedMessageCloseButtonLabel",
            detailedMessageDeleteButtonLabel = "test detailedMessageDeleteButtonLabel",
            emptyStateTitle = "test emptyStateTitle",
            emptyStateDescription = "test emptyStateDescription",
            deleteMessageDialogTitle = "test deleteMessageDialogTitle",
            deleteMessageDialogDescription = "test deleteMessageDialogDescription",
            deleteMessageDialogCancelButtonLabel = "test deleteMessageDialogCancelButtonLabel",
            deleteMessageDialogConfirmButtonLabel = "test deleteMessageDialogConfirmButtonLabel",
            deleteIconButtonAltText = "test deleteIconButtonAltText",
            closeIconButtonAltText = "test closeIconButtonAltText",
            errorStateNoConnectionTitle = "test errorStateNoInternetConnectionTitle",
            errorStateNoConnectionDescription = "test errorStateNoInternetConnectionDescription",
            errorStateNoConnectionRetryButtonLabel = "test errorStateNoInternetConnectionRetryButtonLabel",
            errorStateNoConnectionRefreshIconAltText = "test errorStateNoInternetConnectionRefreshIconAltText",
            snackbarConnectionRestored = "test snackbarConnectionRestored",
            emptyStateFilteredTitle = "test emptyStateFilteredTitle",
            emptyStateFilteredDescription = "test emptyStateFilteredDescription",
            emptyStateFilteredClearFiltersButtonLabel = "test emptyStateFilteredClearFiltersButtonLabel",
            emptyStateFilteredClearFiltersIconAltText = "test emptyStateFilteredClearFiltersIconAltText",
            refreshErrorMessageText = "testRefreshErrorMessageText",
            failedToLoadMoreMessagesText = "testFailedToLoadMoreMessagesText",
            failedToDeleteMessageText = "testFailedToDeleteMessageText"
        )

        val resultStringResources = translationMapper.map(testEmbeddedMessagingContext)

        resultStringResources shouldBe expectedStringResources
    }

    @Test
    fun mapTranslations_should_fallbackForDefaultStringResources_when_embeddedMessagingContextIsNotAvailable() {
        val testEmbeddedMessagingContext = EmbeddedMessagingContext(
            metaData = null
        )

        val result = translationMapper.map(testEmbeddedMessagingContext)

        result shouldBe StringResources()
    }

    private fun createTestLabels(): Labels = Labels(
        allMessagesHeader = "test allMessagesHeader",
        unreadMessagesHeader = "test unreadMessagesHeader",
        filterCategories = "test filterCategories",
        detailedMessageCloseButton = "test detailedMessageCloseButton",
        deleteDetailedMessageButton = "test deleteDetailedMessageButton",
        pinnedMessagesTitle = "test pinnedMessagesTitle",
        emptyStateTitle = "test emptyStateTitle",
        emptyStateDescription = "test emptyStateDescription",
        allMessagesFilterButtonLabel = "test allMessagesFilterButtonLabel",
        unreadMessagesFilterButtonLabel = "test unreadMessagesFilterButtonLabel",
        categoriesFilterButtonLabel = "test categoriesFilterButtonLabel",
        categoriesFilterIconAltText = "test categoriesFilterIconAltText",
        categoriesFilterDialogTitle = "test categoriesFilterDialogTitle",
        categoriesFilterDialogSubtitle = "test categoriesFilterDialogSubtitle",
        selectedCategoryFilterChipIconAltText = "test categoryFilterChipIconAltText",
        categoriesFilterDialogResetButtonLabel = "test categoriesFilterDialogResetButtonLabel",
        categoriesFilterDialogApplyButtonLabel = "test categoriesFilterDialogApplyButtonLabel",
        detailedMessageEmptyStateText = "test detailedMessageEmptyStateText",
        detailedMessageCloseButtonLabel = "test detailedMessageCloseButtonLabel",
        detailedMessageDeleteButtonLabel = "test detailedMessageDeleteButtonLabel",
        deleteMessageDialogTitle = "test deleteMessageDialogTitle",
        deleteMessageDialogDescription = "test deleteMessageDialogDescription",
        deleteMessageDialogCancelButtonLabel = "test deleteMessageDialogCancelButtonLabel",
        deleteMessageDialogConfirmButtonLabel = "test deleteMessageDialogConfirmButtonLabel",
        deleteIconButtonAltText = "test deleteIconButtonAltText",
        closeIconButtonAltText = "test closeIconButtonAltText",
        errorStateNoConnectionTitle = "test errorStateNoInternetConnectionTitle",
        errorStateNoConnectionDescription = "test errorStateNoInternetConnectionDescription",
        snackbarConnectionRestored = "test snackbarConnectionRestored",
        errorStateNoConnectionRetryButtonLabel = "test errorStateNoInternetConnectionRetryButtonLabel",
        errorStateNoConnectionRefreshIconAltText = "test errorStateNoInternetConnectionRefreshIconAltText",
        emptyStateFilteredTitle = "test emptyStateFilteredTitle",
        emptyStateFilteredDescription = "test emptyStateFilteredDescription",
        emptyStateFilteredClearFiltersButtonLabel = "test emptyStateFilteredClearFiltersButtonLabel",
        emptyStateFilteredClearFiltersIconAltText = "test emptyStateFilteredClearFiltersIconAltText",
        refreshErrorMessageText = "testRefreshErrorMessageText",
        failedToLoadMoreMessagesText = "testFailedToLoadMoreMessagesText",
        failedToDeleteMessageText = "testFailedToDeleteMessageText"
    )
}