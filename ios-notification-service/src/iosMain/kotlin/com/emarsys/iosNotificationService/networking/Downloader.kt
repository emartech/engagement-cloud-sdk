package com.emarsys.iosNotificationService.networking

import com.emarsys.iosNotificationService.file.FileSmith
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.Foundation.NSURL
import platform.Foundation.NSURLResponse
import platform.Foundation.NSURLSession
import platform.Foundation.downloadTaskWithURL
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class Downloader(
    private val urlSession: NSURLSession,
    private val fileSmith: FileSmith
) {

    suspend fun download(url: NSURL): NSURL? {
       val result = urlSession.downloadTask(url)
       return result.second.MIMEType?.let { mimeType ->
           return fileSmith.tmpFileUrl(mimeType)?.let { tmpFileUrl ->
               fileSmith.move(result.first, tmpFileUrl)
               return tmpFileUrl
           }
        }
    }

}

suspend fun NSURLSession.downloadTask(url: NSURL): Pair<NSURL, NSURLResponse> = withContext(Dispatchers.Default) {
    suspendCancellableCoroutine { continuation ->
        val task = downloadTaskWithURL(url) { destinationUrl, response, error ->
            if (error != null) {
                continuation.resumeWithException(Throwable(error.description()))
            } else {
                continuation.resume(Pair(destinationUrl!!, response!!))
            }
        }
        task.resume()
    }
}