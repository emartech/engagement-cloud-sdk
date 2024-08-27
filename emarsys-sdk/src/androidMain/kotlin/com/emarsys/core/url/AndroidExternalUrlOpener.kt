package com.emarsys.core.url

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.URLUtil

class AndroidExternalUrlOpener(
    private val applicationContext: Context
) : ExternalUrlOpenerApi {

    override suspend fun open(url: String): Boolean {
        if (!URLUtil.isValidUrl(url)) {
            return false
        }
        val link = Uri.parse(url)
        val externalUrlIntent = Intent(Intent.ACTION_VIEW, link).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            applicationContext.startActivity(externalUrlIntent)
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }
}