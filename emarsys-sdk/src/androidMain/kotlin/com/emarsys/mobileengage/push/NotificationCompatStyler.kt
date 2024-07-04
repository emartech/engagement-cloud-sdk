package com.emarsys.mobileengage.push

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.graphics.drawable.IconCompat
import com.emarsys.core.util.DownloaderApi
import com.emarsys.mobileengage.push.model.AndroidPushMessage
import com.emarsys.mobileengage.push.model.NotificationStyle

class NotificationCompatStyler(
    private val downloader: DownloaderApi
) {

    suspend fun style(
        builder: NotificationCompat.Builder,
        pushMessage: AndroidPushMessage
    ) {
        val image = pushMessage.imageUrlString
            ?.let { downloader.download(it)?.toOptimizedBitmap() }
        val icon = pushMessage.iconUrlString
            ?.let { downloader.download(it)?.toOptimizedBitmap() }

        val notificationStyle =
            when (pushMessage.data.platformData.style) {
                NotificationStyle.BIG_TEXT ->
                    getBigTextStyle(pushMessage)

                NotificationStyle.BIG_PICTURE -> {
                    icon?.let { builder.setLargeIcon(icon) }
                    getBigPictureStyle(image, pushMessage)
                }

                NotificationStyle.MESSAGE -> {
                    getMessagingStyle(image, pushMessage)
                }

                NotificationStyle.THUMBNAIL -> {
                    image?.let { builder.setLargeIcon(it) }
                    getBigTextStyle(pushMessage)
                }

                else -> {
                    if (image != null) {
                        builder.setLargeIcon(image)
                        getBigPictureStyle(image, pushMessage)
                            .bigLargeIcon(null as Bitmap?)
                    } else {
                        getBigTextStyle(pushMessage)
                    }
                }
            }

        builder.setStyle(notificationStyle)
    }

    private fun getMessagingStyle(
        image: Bitmap?,
        pushMessage: AndroidPushMessage,
    ): NotificationCompat.MessagingStyle {
        val user = Person.Builder()
            .setName(pushMessage.title)
            .setIcon(image?.let { IconCompat.createWithAdaptiveBitmap(it) })
            .build()
        return NotificationCompat.MessagingStyle(user)
            .addMessage(
                pushMessage.body,
                System.currentTimeMillis(),
                user
            )
            .setGroupConversation(false)
    }

    private fun getBigPictureStyle(
        image: Bitmap?,
        pushMessage: AndroidPushMessage
    ) = NotificationCompat.BigPictureStyle()
        .bigPicture(image)
        .setBigContentTitle(pushMessage.title)
        .setSummaryText(pushMessage.body)

    private fun getBigTextStyle(pushMessage: AndroidPushMessage) =
        NotificationCompat.BigTextStyle()
            .bigText(pushMessage.body)
            .setBigContentTitle(pushMessage.title)
}

fun ByteArray.toOptimizedBitmap(): Bitmap? {
    return try {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeByteArray(this, 0, size, options)

        val displayMetrics = Resources.getSystem().displayMetrics
        val width = options.outWidth
        var inSampleSize = 1

        while (displayMetrics.widthPixels <= width / inSampleSize) {
            inSampleSize *= 2
        }

        options.inSampleSize = inSampleSize
        options.inJustDecodeBounds = false
        BitmapFactory.decodeByteArray(this, 0, size, options)
    } catch (exception: Exception) {
        null
    }
}
