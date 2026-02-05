package com.emarsys.mobileengage.url

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.URLUtil
import com.emarsys.core.log.Logger
import com.emarsys.core.url.ExternalUrlOpenerApi
import com.emarsys.mobileengage.action.models.OpenExternalUrlActionModel
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

class AndroidExternalUrlOpener(
    private val applicationContext: Context,
    private val sdkLogger: Logger
) : ExternalUrlOpenerApi {

    override suspend fun open(actionModel: OpenExternalUrlActionModel) {
        val url = actionModel.url
        if (URLUtil.isValidUrl(url)) {
            val link = Uri.parse(url)
            val externalUrlIntent = Intent(Intent.ACTION_VIEW, link).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                applicationContext.startActivity(externalUrlIntent)
            } catch (exception: ActivityNotFoundException) {

                sdkLogger.error(
                    "Failed to open url: $url",
                    exception,
                    buildJsonObject { put("url", JsonPrimitive(url)) })
            }
        }
    }
}