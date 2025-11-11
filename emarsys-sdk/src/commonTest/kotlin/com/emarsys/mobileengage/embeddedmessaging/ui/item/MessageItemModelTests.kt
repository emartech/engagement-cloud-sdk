package com.emarsys.mobileengage.embeddedmessaging.ui.item

import com.emarsys.core.util.DownloaderApi
import com.emarsys.mobileengage.action.models.PresentableActionModel
import com.emarsys.mobileengage.embeddedmessaging.provider.FallbackImageProviderApi
import com.emarsys.networking.clients.embedded.messaging.model.EmbeddedMessage
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class MessageItemModelTests {

    private lateinit var mockDownloader: DownloaderApi
    private lateinit var mockFallbackImageProvider: FallbackImageProviderApi

    @BeforeTest
    fun setup() {
        mockDownloader = mock()
        mockFallbackImageProvider = mock()
    }

    @Test
    fun downloadImage_shouldReturn_Null_when_ImageUrlIsMissing() = runTest {
        val testMessage = EmbeddedMessage(
            id = "1",
            title = "testTitle",
            lead = "testLead",
            imageUrl = null,
            defaultAction = null,
            actions = emptyList<PresentableActionModel>(),
            tags = emptyList(),
            categoryIds = emptyList(),
            receivedAt = 100000L,
            expiresAt = 110000L,
            properties = emptyMap(),
            trackingInfo = "anything"
        )
        val model = MessageItemModel(message = testMessage, downloaderApi = mockDownloader, fallbackImageProvider = mockFallbackImageProvider)

        val result = model.downloadImage()

        result shouldBe null
        verifySuspend(VerifyMode.exactly(0)) { mockDownloader }
    }

    @Test
    fun downloadImage_shouldCall_downloaderApi() = runTest {
        val testMessage = EmbeddedMessage(
            id = "1",
            title = "testTitle",
            lead = "testLead",
            imageUrl = "example.com",
            defaultAction = null,
            actions = emptyList<PresentableActionModel>(),
            tags = emptyList(),
            categoryIds = emptyList(),
            receivedAt = 100000L,
            expiresAt = 110000L,
            properties = emptyMap(),
            trackingInfo = "anything"
        )
        val model = MessageItemModel(message = testMessage, downloaderApi = mockDownloader, fallbackImageProvider = mockFallbackImageProvider)
        everySuspend { mockDownloader.download("example.com") }.returns(ByteArray(0))

        model.downloadImage()

        verifySuspend(VerifyMode.exactly(1)) { mockDownloader }
    }
}