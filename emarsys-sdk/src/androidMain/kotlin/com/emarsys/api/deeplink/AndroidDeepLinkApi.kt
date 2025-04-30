package com.emarsys.api.deeplink

import android.app.Activity
import android.content.Intent

interface AndroidDeepLinkApi {
    suspend fun trackDeepLink(activity: Activity, intent: Intent)
}