package com.emarsys.mobileengage.embeddedmessaging.ui.item

import com.emarsys.mobileengage.action.models.PresentableActionModel
import com.emarsys.networking.clients.embedded.messaging.model.EmbeddedMessage
import com.emarsys.networking.clients.embedded.messaging.model.ListThumbnailImage
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test

class MessageItemViewModelTests {

    private lateinit var mockMessageItemModel: MessageItemModelApi

    @BeforeTest
    fun setup() = runTest {
        mockMessageItemModel = mock(MockMode.autofill)
    }

    @Test
    fun id_shouldReturn_MessageId() {
        val testMessage = createTestMessage(id = "testId")
        val mockModel = mock<MessageItemModelApi>(MockMode.autofill)
        every { mockModel.message } returns testMessage
        val viewModel = MessageItemViewModel(mockModel)

        val result = viewModel.id

        result shouldBe "testId"
    }

    @Test
    fun title_shouldReturn_MessageTitle() {
        val testMessage = createTestMessage(title = "Test Title")
        val mockModel = mock<MessageItemModelApi>(MockMode.autofill)
        every { mockModel.message } returns testMessage
        val viewModel = MessageItemViewModel(mockModel)

        val result = viewModel.title

        result shouldBe "Test Title"
    }

    @Test
    fun lead_shouldReturn_MessageLead() {
        val testMessage = createTestMessage(lead = "Test Lead")
        val mockModel = mock<MessageItemModelApi>(MockMode.autofill)
        every { mockModel.message } returns testMessage
        val viewModel = MessageItemViewModel(mockModel)

        val result = viewModel.lead

        result shouldBe "Test Lead"
    }

    @Test
    fun imageUrl_shouldReturn_MessageImageUrl() {
        val testMessage = createTestMessage(imageUrl = "https://example.com/image.jpg")
        val mockModel = mock<MessageItemModelApi>(MockMode.autofill)
        every { mockModel.message } returns testMessage
        val viewModel = MessageItemViewModel(mockModel)

        val result = viewModel.imageUrl

        result shouldBe "https://example.com/image.jpg"
    }

    @Test
    fun imageUrl_shouldReturn_Null_when_MessageImageUrlIsNull() {
        val testMessage = createTestMessage(imageUrl = null)
        val mockModel = mock<MessageItemModelApi>(MockMode.autofill)
        every { mockModel.message } returns testMessage
        val viewModel = MessageItemViewModel(mockModel)

        val result = viewModel.imageUrl

        result shouldBe null
    }

    @Test
    fun receivedAt_shouldReturn_MessageReceivedAt() {
        val testMessage = createTestMessage(receivedAt = 1234567890L)
        val mockModel = mock<MessageItemModelApi>(MockMode.autofill)
        every { mockModel.message } returns testMessage
        val viewModel = MessageItemViewModel(mockModel)

        val result = viewModel.receivedAt

        result shouldBe 1234567890L
    }

    @Test
    @Ignore
    fun fetchImage_shouldCallDownloadImage_onTheModel() = runTest {
        val imageByteArray = byteArrayOf()
        val testMessage = createTestMessage(imageUrl = "testUrl")
        val mockModel = mock<MessageItemModelApi>(MockMode.autofill)
        every { mockModel.message } returns testMessage
        everySuspend { mockModel.downloadImage() } returns imageByteArray

        val viewModel = MessageItemViewModel(mockModel)

        viewModel.fetchImage()

        verifySuspend { mockModel.downloadImage() }
    }

    private fun createTestMessage(
        id: String = "1",
        title: String = "testTitle",
        lead: String = "testLead",
        imageUrl: String? = null,
        receivedAt: Long = 100000L
    ): EmbeddedMessage {
        return EmbeddedMessage(
            id = id,
            title = title,
            lead = lead,
            listThumbnailImage = imageUrl?.let {
                ListThumbnailImage(imageUrl, null)
            },
            defaultAction = null,
            actions = emptyList<PresentableActionModel>(),
            tags = emptyList(),
            categoryIds = emptyList(),
            receivedAt = receivedAt,
            expiresAt = 110000L,
            properties = emptyMap(),
            trackingInfo = "anything"
        )
    }
}