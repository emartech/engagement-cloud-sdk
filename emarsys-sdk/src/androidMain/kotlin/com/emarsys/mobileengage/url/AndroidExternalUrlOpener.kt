package com.emarsys.mobileengage.url

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.URLUtil
import com.emarsys.core.log.Logger
import com.emarsys.core.url.ExternalUrlOpenerApi

class AndroidExternalUrlOpener(
    private val applicationContext: Context,
    private val sdkLogger: Logger
) : ExternalUrlOpenerApi {

    override suspend fun open(url: String) {
        if (URLUtil.isValidUrl(url)) {
            val link = Uri.parse(url)
            val externalUrlIntent = Intent(Intent.ACTION_VIEW, link).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                applicationContext.startActivity(externalUrlIntent)
            } catch (exception: ActivityNotFoundException) {
                sdkLogger.error("AndroidExternalUrlOpener", exception, mapOf("url" to url))
            }
        }
    }
}