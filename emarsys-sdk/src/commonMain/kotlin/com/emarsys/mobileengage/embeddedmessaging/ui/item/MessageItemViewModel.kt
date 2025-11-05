package com.emarsys.mobileengage.embeddedmessaging.ui.item

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap
import com.emarsys.core.util.DownloaderApi
import com.emarsys.emarsys_sdk.generated.resources.Res

class MessageItemViewModel(private val downloader: DownloaderApi) {

    suspend fun fetchImage(url: String): ImageBitmap =
        downloader.download(url)?.decodeToImageBitmap() ?: getFallbackImage()


    private suspend fun getFallbackImage(): ImageBitmap {
        return Res.readBytes("files/placeholder.png").decodeToImageBitmap()
    }
}