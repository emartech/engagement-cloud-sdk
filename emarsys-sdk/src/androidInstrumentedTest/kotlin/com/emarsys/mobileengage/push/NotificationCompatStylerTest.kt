package com.emarsys.mobileengage.push

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.Builder
import com.emarsys.core.util.DownloaderApi
import com.emarsys.mobileengage.push.model.AndroidPlatformData
import com.emarsys.mobileengage.push.model.AndroidPushMessage
import com.emarsys.mobileengage.push.model.NotificationMethod
import com.emarsys.mobileengage.push.model.NotificationOperation
import com.emarsys.mobileengage.push.model.NotificationStyle
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class NotificationCompatStylerTest {

    private val styleSlot = slot<NotificationCompat.Style>()

    private lateinit var mockNotificationBuilder: Builder

    private lateinit var mockDownloader: DownloaderApi

    private lateinit var notificationCompatStyler: NotificationCompatStyler

    companion object {
        const val CHANNEL_ID = "channelId"
        const val IMAGE_URL = "imageUrl"
        const val ICON_URL = "iconUrl"
    }

    @Before
    fun setup() {
        mockkStatic(BitmapFactory::class)
        mockNotificationBuilder = mockk(relaxed = true)
        every { mockNotificationBuilder.setStyle(capture(styleSlot)) } returns mockNotificationBuilder
        mockDownloader = mockk(relaxed = true)
        notificationCompatStyler = NotificationCompatStyler(mockDownloader)
    }

    @After
    fun tearDown() {
        unmockkAll()
        styleSlot.clear()
    }

    @Test
    fun style_shouldCreateBigTextStyle_whenNotificationStyleIsBigText() = runTest {
        val pushMessage = createPushMessage(NotificationStyle.BIG_TEXT)

        notificationCompatStyler.style(
            mockNotificationBuilder, pushMessage
        )

        verify { mockNotificationBuilder.setStyle(any()) }
        (styleSlot.captured is NotificationCompat.BigTextStyle) shouldBe true
    }

    @Test
    fun style_shouldCreateBigPictureStyle_andSetLargeIcon_whenNotificationStyleIsBigPictureAndIconIsNotNull() =
        runTest {
            val pushMessage =
                createPushMessage(NotificationStyle.BIG_PICTURE, iconUrlString = ICON_URL)
            val expectedIcon = mockImageDownload(pushMessage.iconUrlString!!)

            notificationCompatStyler.style(
                mockNotificationBuilder, pushMessage
            )

            verify { mockNotificationBuilder.setLargeIcon(expectedIcon) }
            verify { mockNotificationBuilder.setStyle(any()) }
            (styleSlot.captured is NotificationCompat.BigPictureStyle) shouldBe true
        }

    @Test
    fun style_shouldCreateBigPictureStyle_andNotSetLargeIcon_whenNotificationStyleIsBigPictureAndIconIsNull() =
        runTest {
            val pushMessage =
                createPushMessage(NotificationStyle.BIG_PICTURE, iconUrlString = null)

            notificationCompatStyler.style(
                mockNotificationBuilder, pushMessage
            )

            verify(exactly = 0) { mockNotificationBuilder.setLargeIcon(any<Bitmap>()) }
            verify { mockNotificationBuilder.setStyle(any()) }
            (styleSlot.captured is NotificationCompat.BigPictureStyle) shouldBe true
        }

    @Test
    fun style_shouldCreateMessagingStyle_whenNotificationStyleIsMessage() = runTest {
        val pushMessage = createPushMessage(NotificationStyle.MESSAGE)

        notificationCompatStyler.style(
            mockNotificationBuilder, pushMessage
        )

        verify { mockNotificationBuilder.setStyle(any()) }
        (styleSlot.captured is NotificationCompat.MessagingStyle) shouldBe true
    }

    @Test
    fun style_shouldCreateBigTextStyle_andSetImageAsLargeIcon_whenNotificationStyleIsThumbnail() =
        runTest {
            val pushMessage =
                createPushMessage(NotificationStyle.THUMBNAIL, imageUrlString = IMAGE_URL)
            val expectedImage = mockImageDownload(pushMessage.imageUrlString!!)

            notificationCompatStyler.style(
                mockNotificationBuilder, pushMessage
            )

            verify { mockNotificationBuilder.setLargeIcon(expectedImage) }
            verify { mockNotificationBuilder.setStyle(any()) }
            (styleSlot.captured is NotificationCompat.BigTextStyle) shouldBe true
        }

    @Test
    fun style_shouldCreateBigTextStyle_andNotSetLargeIcon_whenNotificationStyleIsThumbnailAndImageIsNull() =
        runTest {
            val pushMessage =
                createPushMessage(NotificationStyle.THUMBNAIL, imageUrlString = null)

            notificationCompatStyler.style(
                mockNotificationBuilder, pushMessage
            )

            verify(exactly = 0) { mockNotificationBuilder.setLargeIcon(any<Bitmap>()) }
            verify { mockNotificationBuilder.setStyle(any()) }
            (styleSlot.captured is NotificationCompat.BigTextStyle) shouldBe true
        }

    @Test
    fun style_shouldCreateBigTextStyle_whenNotificationStyleIsNotSet_andImageUrlIsNull() =
        runTest {
            val pushMessage =
                createPushMessage(style = null, imageUrlString = null)

            notificationCompatStyler.style(
                mockNotificationBuilder, pushMessage
            )

            verify { mockNotificationBuilder.setStyle(any()) }
            (styleSlot.captured is NotificationCompat.BigTextStyle) shouldBe true
        }

    @Test
    fun style_shouldCreateBigPictureStyle_andSetImagesAsLargeIcon_whenNotificationStyleIsNotSet_andImageUrlIsNotNull() =
        runTest {
            val pushMessage =
                createPushMessage(style = null, imageUrlString = IMAGE_URL)
            val expectedImage = mockImageDownload(pushMessage.imageUrlString!!)

            notificationCompatStyler.style(
                mockNotificationBuilder, pushMessage
            )

            verify { mockNotificationBuilder.setStyle(any()) }
            verify { mockNotificationBuilder.setLargeIcon(expectedImage) }
            (styleSlot.captured is NotificationCompat.BigPictureStyle) shouldBe true
        }


    private fun createPushMessage(
        style: NotificationStyle? = null,
        iconUrlString: String? = null,
        imageUrlString: String? = null
    ) = AndroidPushMessage(
        "collapseId",
        "title",
        "body",
        iconUrlString = iconUrlString,
        imageUrlString = imageUrlString,
        data = PushData(
            campaignId = "campaignId",
            sid = "sid",
            platformData = AndroidPlatformData(
                CHANNEL_ID,
                NotificationMethod("collapseId", NotificationOperation.INIT),
                style = style
            )
        )
    )

    private fun mockImageDownload(urlString: String): Bitmap {
        val imageByteArray = byteArrayOf(1, 2, 3)
        val expectedImage = mockk<Bitmap>(relaxed = true)
        coEvery { mockDownloader.download(urlString) } returns imageByteArray
        every {
            BitmapFactory.decodeByteArray(
                eq(imageByteArray),
                eq(0),
                any(),
                any()
            )
        } returns expectedImage
        return expectedImage
    }

}