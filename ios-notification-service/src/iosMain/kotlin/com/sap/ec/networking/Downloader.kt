package com.sap.ec.iosNotificationService.networking

import com.sap.ec.iosNotificationService.file.FileSmith
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.NSURLResponse
import platform.Foundation.NSURLSession
import platform.Foundation.dataTaskWithURL
import platform.Foundation.downloadTaskWithURL
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class Downloader(
    private val urlSession: NSURLSession,
    private val fileSmith: FileSmith
) {

    suspend fun downloadFile(url: NSURL): NSURL? {
       val result = urlSession.downloadTask(url)
       return result.second.MIMEType?.let { mimeType ->
           return fileSmith.tmpFileUrl(mimeType)?.let { tmpFileUrl ->
               fileSmith.move(result.first, tmpFileUrl)
               return tmpFileUrl
           }
        }
    }

    suspend fun downloadData(url: NSURL): NSData? {
        return urlSession.dataTask(url)
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

suspend fun NSURLSession.dataTask(url: NSURL): NSData? = withContext(Dispatchers.Default) {
    suspendCancellableCoroutine { continuation ->
        val task = dataTaskWithURL(url) { data, _, error ->
            if (error != null) {
                continuation.resumeWithException(Throwable(error.description()))
            } else {
                continuation.resume(data!!)
            }
        }
        task.resume()
    }
}
