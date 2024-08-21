package com.emarsys.core.url

import android.content.Intent
import android.net.Uri
import com.emarsys.applicationContext

class AndroidExternalUrlOpener : ExternalUrlOpenerApi {
    override fun open(url: String) {
        val link = Uri.parse(url)
        val externalUrlIntent = Intent(Intent.ACTION_VIEW, link)
        externalUrlIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        applicationContext.startActivity(externalUrlIntent)
    }
}