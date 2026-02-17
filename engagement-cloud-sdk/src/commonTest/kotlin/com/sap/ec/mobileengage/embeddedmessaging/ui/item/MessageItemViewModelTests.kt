package com.sap.ec.mobileengage.embeddedmessaging.ui.item

import com.sap.ec.mobileengage.action.models.PresentableActionModel
import com.sap.ec.networking.clients.embedded.messaging.model.EmbeddedMessage
import com.sap.ec.networking.clients.embedded.messaging.model.ListThumbnailImage
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.equals.shouldNotBeEqual
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class MessageItemViewModelTests {
    private companion object {
        const val TRACKING_INFO = """{"trackingKey":"trackingValue"}"""
    }

    private lateinit var mockMessageItemModel: MessageItemModelApi

    private lateinit var viewModel: MessageItemViewModel

    @BeforeTest
    fun setup() {
        mockMessageItemModel = mock()
        viewModel = MessageItemViewModel(mockMessageItemModel)
    }

    @Test
    fun id_shouldReturn_ModelId() {
        val testMessage = createTestMessage(id = "testId")
        every { mockMessageItemModel.message } returns testMessage

        val result = viewModel.id

        result shouldBe "testId"
    }

    @Test
    fun title_shouldReturn_ModelTitle() {
        val testMessage = createTestMessage(title = "Test Title")
        every { mockMessageItemModel.message } returns testMessage

        val result = viewModel.title

        result shouldBe "Test Title"
    }

    @Test
    fun lead_shouldReturn_ModelLead() {
        val testMessage = createTestMessage(lead = "Test Lead")
        every { mockMessageItemModel.message } returns testMessage

        val result = viewModel.lead

        result shouldBe "Test Lead"
    }

    @Test
    fun imageUrl_shouldReturn_ModelImageUrl() {
        val testMessage = createTestMessage(imageUrl = "https://example.com/image.jpg")
        every { mockMessageItemModel.message } returns testMessage

        val result = viewModel.imageUrl

        result shouldBe "https://example.com/image.jpg"
    }

    @Test
    fun imageUrl_shouldReturn_ModelImageAltText() {
        val expectedAltText = "Example Image"
        val testMessage = createTestMessage(
            imageUrl = "https://example.com/image.jpg",
            imageAltText = expectedAltText
        )
        every { mockMessageItemModel.message } returns testMessage

        val result = viewModel.imageAltText

        result shouldBe expectedAltText
    }

    @Test
    fun imageUrl_shouldReturn_Null_when_ModelImageUrlIsNull() {
        val testMessage = createTestMessage(imageUrl = null)
        every { mockMessageItemModel.message } returns testMessage

        val result = viewModel.imageUrl

        result shouldBe null
    }

    @Test
    fun receivedAt_shouldReturn_ModelReceivedAt() {
        val testMessage = createTestMessage(receivedAt = 1234567890L)
        every { mockMessageItemModel.message } returns testMessage

        val result = viewModel.receivedAt

        result shouldBe 1234567890L
    }

    @Test
    fun trackingInfo_shouldReturn_ModelTrackingInfo() {
        val testMessage = createTestMessage(TRACKING_INFO)
        every { mockMessageItemModel.message } returns testMessage

        viewModel.trackingInfo shouldBe TRACKING_INFO
    }

    @Test
    fun isNotOpened_shouldReturn_ModelIsNotOpened() {
        every { mockMessageItemModel.isNotOpened() } returns true

        viewModel.isNotOpened shouldBe true
    }

    @Test
    fun isRead_shouldReturn_ModelIsRead() {
        every { mockMessageItemModel.isRead() } returns true

        viewModel.isRead shouldBe true
    }

    @Test
    fun isPinned_shouldReturn_ModelIsPinned() {
        every { mockMessageItemModel.isPinned() } returns true

        viewModel.isPinned shouldBe true
    }

    @Test
    fun copyAsExcludedLocally_shouldCreateNewInstance_withUpdatedIsExcludedLocally() = runTest {
        val result = viewModel.copyAsExcludedLocally()

        result shouldNotBeEqual viewModel
        result.isExcludedLocally shouldBe true
    }

    @Test
    fun copyAsExcludedLocally_shouldCreateNewInstance_andLeaveExcludedLocallyAsTrue() = runTest {
        val copiedViewModel = viewModel.copyAsExcludedLocally()

        val result = copiedViewModel.copyAsExcludedLocally()

        result shouldNotBeEqual viewModel
        result shouldNotBeEqual copiedViewModel
        copiedViewModel.isExcludedLocally shouldBe true
        result.isExcludedLocally shouldBe true
    }

    @Test
    fun hasRichContent_shouldReturn_ModelHasRichContentToDetailView() {
        every { mockMessageItemModel.hasRichContent() } returns true

        viewModel.hasRichContent() shouldBe true
    }

    @Test
    fun handleDefaultAction_shouldCall_ModelHandleDefaultAction() = runTest {
        everySuspend { mockMessageItemModel.handleDefaultAction() } returns Unit

        viewModel.handleDefaultAction()

        verifySuspend { mockMessageItemModel.handleDefaultAction() }
    }

    @Test
    fun tagMessageRead_shouldCall_ModelTagMessageOpened() = runTest {
        everySuspend { mockMessageItemModel.tagMessageOpened() } returns Result.success(Unit)

        val result = viewModel.tagMessageOpened()

        result.isSuccess shouldBe true
        verifySuspend { mockMessageItemModel.tagMessageOpened() }
    }

    @Test
    fun deleteMessage_shouldCall_ModelDeleteMessage() = runTest {
        everySuspend { mockMessageItemModel.deleteMessage() } returns Result.success(Unit)

        val result = viewModel.deleteMessage()

        result.isSuccess shouldBe true
        verifySuspend { mockMessageItemModel.deleteMessage() }
    }

    @Test
    fun deleteMessage_shouldCall_ModelTagMessageRead() = runTest {
        everySuspend { mockMessageItemModel.tagMessageRead() } returns Result.success(Unit)

        val result = viewModel.tagMessageRead()

        result.isSuccess shouldBe true
        verifySuspend { mockMessageItemModel.tagMessageRead() }
    }

    @Test
    fun fetchImage_shouldCallDownloadImage_onTheModel() = runTest {
        val imageByteArray = byteArrayOf()
        val testMessage = createTestMessage(imageUrl = "testUrl")
        every { mockMessageItemModel.message } returns testMessage
        everySuspend { mockMessageItemModel.downloadImage() } returns imageByteArray

        viewModel.fetchImage()

        verifySuspend { mockMessageItemModel.downloadImage() }
    }

    private fun createTestMessage(
        id: String = "1",
        title: String = "testTitle",
        lead: String = "testLead",
        imageUrl: String? = null,
        imageAltText: String? = null,
        receivedAt: Long = 100000L,
        trackingInfo: String = TRACKING_INFO
    ): EmbeddedMessage {
        return EmbeddedMessage(
            id = id,
            title = title,
            lead = lead,
            listThumbnailImage = imageUrl?.let {
                ListThumbnailImage(imageUrl, imageAltText)
            },
            defaultAction = null,
            actions = emptyList<PresentableActionModel>(),
            tags = emptyList(),
            categories = emptyList(),
            receivedAt = receivedAt,
            expiresAt = 110000L,
            properties = emptyMap(),
            trackingInfo = trackingInfo
        )
    }
}