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
            allMessagesFilterButtonAltText = "test allMessagesFilterButtonAltText",
            unreadMessagesFilterButtonLabel = "test unreadMessagesFilterButtonLabel",
            unreadMessagesFilterButtonAltText = "test unreadMessagesFilterButtonAltText",
            categoriesFilterButtonLabel = "test categoriesFilterButtonLabel",
            categoriesFilterIconAltText = "test categoriesFilterIconAltText",
            categoriesFilterDialogTitle = "test categoriesFilterDialogTitle",
            categoriesFilterDialogSubtitle = "test categoriesFilterDialogSubtitle",
            categoryFilterChipIconAltText = "test categoryFilterChipIconAltText",
            categoriesFilterDialogResetButtonLabel = "test categoriesFilterDialogResetButtonLabel",
            categoriesFilterDialogResetButtonAltText = "test categoriesFilterDialogResetButtonAltText",
            categoriesFilterDialogApplyButtonLabel = "test categoriesFilterDialogApplyButtonLabel",
            categoriesFilterDialogCloseButtonAltText = "test categoriesFilterDialogCloseButtonAltText",
            pinnedMessagesTitle = "test pinnedMessagesTitle",
            detailedMessageCloseButtonLabel = "test detailedMessageCloseButtonLabel",
            detailedMessageCloseButtonAltText = "test detailedMessageCloseButtonAltText",
            detailedMessageDeleteButtonLabel = "test detailedMessageDeleteButtonLabel",
            detailedMessageDeleteButtonAltText = "test detailedMessageDeleteButtonAltText",
            emptyStateTitle = "test emptyStateTitle",
            emptyStateDescription = "test emptyStateDescription",
            deleteMessageDialogTitle = "test deleteMessageDialogTitle",
            deleteMessageDialogDescription = "test deleteMessageDialogDescription",
            deleteMessageDialogCancelButtonLabel = "test deleteMessageDialogCancelButtonLabel",
            deleteMessageDialogCancelButtonAltText = "test deleteMessageDialogCancelButtonAltText",
            deleteMessageDialogConfirmButtonLabel = "test deleteMessageDialogConfirmButtonLabel",
            deleteMessageDialogConfirmButtonAltText = "test deleteMessageDialogConfirmButtonAltText",
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
        allMessagesFilterButtonAltText = "test allMessagesFilterButtonAltText",
        unreadMessagesFilterButtonLabel = "test unreadMessagesFilterButtonLabel",
        unreadMessagesFilterButtonAltText = "test unreadMessagesFilterButtonAltText",
        categoriesFilterButtonLabel = "test categoriesFilterButtonLabel",
        categoriesFilterIconAltText = "test categoriesFilterIconAltText",
        categoriesFilterDialogTitle = "test categoriesFilterDialogTitle",
        categoriesFilterDialogSubtitle = "test categoriesFilterDialogSubtitle",
        categoryFilterChipIconAltText = "test categoryFilterChipIconAltText",
        categoriesFilterDialogResetButtonLabel = "test categoriesFilterDialogResetButtonLabel",
        categoriesFilterDialogResetButtonAltText = "test categoriesFilterDialogResetButtonAltText",
        categoriesFilterDialogApplyButtonLabel = "test categoriesFilterDialogApplyButtonLabel",
        categoriesFilterDialogCloseButtonAltText = "test categoriesFilterDialogCloseButtonAltText",
        detailedMessageCloseButtonLabel = "test detailedMessageCloseButtonLabel",
        detailedMessageCloseButtonAltText = "test detailedMessageCloseButtonAltText",
        detailedMessageDeleteButtonLabel = "test detailedMessageDeleteButtonLabel",
        detailedMessageDeleteButtonAltText = "test detailedMessageDeleteButtonAltText",
        deleteMessageDialogTitle = "test deleteMessageDialogTitle",
        deleteMessageDialogDescription = "test deleteMessageDialogDescription",
        deleteMessageDialogCancelButtonLabel = "test deleteMessageDialogCancelButtonLabel",
        deleteMessageDialogCancelButtonAltText = "test deleteMessageDialogCancelButtonAltText",
        deleteMessageDialogConfirmButtonLabel = "test deleteMessageDialogConfirmButtonLabel",
        deleteMessageDialogConfirmButtonAltText = "test deleteMessageDialogConfirmButtonAltText",
    )
}