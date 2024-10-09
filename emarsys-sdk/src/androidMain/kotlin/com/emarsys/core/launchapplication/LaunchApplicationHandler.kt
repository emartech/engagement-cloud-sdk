package com.emarsys.core.launchapplication

import android.content.Context
import android.content.Intent
import com.emarsys.AndroidEmarsysConfig
import com.emarsys.context.SdkContextApi
import com.emarsys.watchdog.activity.ActivityFinderApi

class LaunchApplicationHandler(
    private val applicationContext: Context,
    private val activityFinder: ActivityFinderApi,
    private val sdkContext: SdkContextApi
): LaunchApplicationHandlerApi {

    override suspend fun launchApplication() {
        if (activityFinder.currentActivity() == null) {
            val config = sdkContext.config as? AndroidEmarsysConfig
            val intent = if (config?.launchActivityClass != null) {
                Intent(applicationContext, config.launchActivityClass)
            } else {
                applicationContext.packageManager.getLaunchIntentForPackage(applicationContext.packageName)
            }
            applicationContext.startActivity(intent)
            activityFinder.waitForActivity()
        }
    }
}
