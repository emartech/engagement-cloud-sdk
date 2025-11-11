package com.emarsys.mobileengage.embeddedmessaging.ui.item

import com.emarsys.mobileengage.action.models.PresentableActionModel
import com.emarsys.mobileengage.embeddedmessaging.provider.FallbackImageProviderApi
import com.emarsys.networking.clients.embedded.messaging.model.EmbeddedMessage
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import dev.mokkery.verify.VerifyMode
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test

class MessageItemViewModelTests {

    private lateinit var mockMessageItemModel: MessageItemModelApi
    private lateinit var mockFallbackImageProvider: FallbackImageProviderApi
    private val fallbackImageBytes = ByteArray(100) { it.toByte() }

    @BeforeTest
    fun setup() = runTest {
        mockMessageItemModel = mock(MockMode.autofill)
        mockFallbackImageProvider = mock(MockMode.autofill)
        everySuspend { mockMessageItemModel.getFallbackImageProvider() } returns mockFallbackImageProvider
        everySuspend { mockFallbackImageProvider.provide() } returns fallbackImageBytes
    }

    @Ignore
    @Test
    fun fetchImage_should_ReturnFallbackImage_when_downloadImageReturnsNull() = runTest {
        everySuspend {
            mockMessageItemModel.downloadImage()
        } returns null
        val viewModel = MessageItemViewModel(mockMessageItemModel)

        val result = viewModel.fetchImage()

        result shouldNotBe null
        verifySuspend(VerifyMode.exactly(1)) { mockMessageItemModel.downloadImage() }
        verifySuspend(VerifyMode.exactly(1)) { mockMessageItemModel.getFallbackImageProvider() }
        verifySuspend(VerifyMode.exactly(1)) { mockFallbackImageProvider.provide() }
    }

    @Ignore
    @Test
    fun fetchImage_should_ReturnDownloadedImage_when_downloadImageSucceeds() = runTest {
        val testImageBytes = ByteArray(100) { it.toByte() }
        everySuspend {
            mockMessageItemModel.downloadImage()
        } returns testImageBytes
        val viewModel = MessageItemViewModel(mockMessageItemModel)

        val result = viewModel.fetchImage()

        result shouldNotBe null
        verifySuspend(VerifyMode.exactly(1)) { mockMessageItemModel.downloadImage() }
        verifySuspend(VerifyMode.exactly(0)) { mockMessageItemModel.getFallbackImageProvider() }
    }


    @Ignore
    @Test
    fun fetchImage_should_ReturnFallbackImage_when_downloadImageThrowsException() = runTest {
        everySuspend {
            mockMessageItemModel.downloadImage()
        } throws RuntimeException("Network error")
        val viewModel = MessageItemViewModel(mockMessageItemModel)

        val result = viewModel.fetchImage()

        result shouldNotBe null
        verifySuspend(VerifyMode.exactly(1)) { mockMessageItemModel.downloadImage() }
        verifySuspend(VerifyMode.exactly(1)) { mockMessageItemModel.getFallbackImageProvider() }
        verifySuspend(VerifyMode.exactly(1)) { mockFallbackImageProvider.provide() }
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
            imageUrl = imageUrl,
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