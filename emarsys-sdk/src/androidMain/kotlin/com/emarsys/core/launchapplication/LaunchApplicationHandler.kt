package com.emarsys.core.launchapplication

import android.content.Context
import android.content.Intent
import com.emarsys.AndroidEmarsysConfig
import com.emarsys.SdkConfig
import com.emarsys.watchdog.activity.ActivityFinderApi

class LaunchApplicationHandler(
    private val applicationContext: Context,
    private val activityFinder: ActivityFinderApi): LaunchApplicationHandlerApi {

    override suspend fun launchApplication(config: SdkConfig) {
        if (activityFinder.currentActivity() == null) {
            val intent = if (config is AndroidEmarsysConfig && config.launchActivityClass != null) {
                Intent(applicationContext, config.launchActivityClass)
            } else {
                applicationContext.packageManager.getLaunchIntentForPackage(applicationContext.packageName)
            }
            applicationContext.startActivity(intent)
            activityFinder.waitForActivity()
        }
    }
}
