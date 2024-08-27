package com.emarsys.core.url

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

class IosExternalUrlOpener(
    private val uiApplication: UIApplication,
    private val mainDispatcher: CoroutineDispatcher,
) : ExternalUrlOpenerApi {

    override suspend fun open(url: String): Boolean {
        if (uiApplication.canOpenURL(NSURL(string = url))) {
            return withContext(mainDispatcher) {
                uiApplication.openURL(url = NSURL(string = url))
            }
        }
        return false
    }

}