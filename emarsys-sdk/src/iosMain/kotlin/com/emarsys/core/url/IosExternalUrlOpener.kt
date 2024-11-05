package com.emarsys.core.url

import com.emarsys.core.log.LogEntry
import com.emarsys.core.log.LogLevel
import com.emarsys.core.log.Logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

class IosExternalUrlOpener(
    private val uiApplication: UIApplication,
    private val mainDispatcher: CoroutineDispatcher,
    private val sdkLogger: Logger
) : ExternalUrlOpenerApi {

    override suspend fun open(url: String) {
        val nsUrl = NSURL(string = url)
        if (uiApplication.canOpenURL(nsUrl)) {
            withContext(mainDispatcher) {
                uiApplication.openURL(nsUrl, emptyMap<Any?, Any?>()) { success ->
                    if (!success) {
                        sdkLogger.log(
                            LogEntry(
                                "IosExternalUrlOpener",
                                mapOf("message" to "Failed to open url: $url")
                            ), LogLevel.Error
                        )
                    }
                }
            }
        }
    }

}