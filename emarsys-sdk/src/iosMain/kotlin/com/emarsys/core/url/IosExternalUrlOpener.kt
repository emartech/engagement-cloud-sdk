package com.emarsys.core.url

import com.emarsys.core.log.Logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

class IosExternalUrlOpener(
    private val uiApplication: UIApplication,
    private val mainDispatcher: CoroutineDispatcher,
    private val sdkDispatcher: CoroutineDispatcher,
    private val sdkLogger: Logger
) : ExternalUrlOpenerApi {

    override suspend fun open(url: String) {
        val nsUrl = NSURL(string = url)
        if (uiApplication.canOpenURL(nsUrl)) {
            withContext(mainDispatcher) {
                uiApplication.openURL(nsUrl, emptyMap<Any?, Any?>()) { success ->
                    if (!success) {
                        CoroutineScope(sdkDispatcher).launch {
                            sdkLogger.error(
                                "IosExternalUrlOpener",
                                buildJsonObject {
                                    put(
                                        "message",
                                        JsonPrimitive("Failed to open url: $url")
                                    )
                                }

                            )
                        }
                    }
                }
            }
        }
    }

}