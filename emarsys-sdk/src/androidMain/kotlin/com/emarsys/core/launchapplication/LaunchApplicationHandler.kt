package com.emarsys.core.launchapplication

import android.app.ActivityOptions
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import com.emarsys.AndroidEmarsysConfig
import com.emarsys.context.SdkContextApi
import com.emarsys.core.actions.launchapplication.LaunchApplicationHandlerApi
import com.emarsys.core.device.AndroidVersionUtils.isUpsideDownCake
import com.emarsys.core.device.AndroidVersionUtils.isUpsideDownCakeOrAbove
import com.emarsys.core.log.Logger
import com.emarsys.watchdog.activity.ActivityFinderApi

class LaunchApplicationHandler(
    private val applicationContext: Context,
    private val activityFinder: ActivityFinderApi,
    private val sdkContext: SdkContextApi,
    private val sdkLogger: Logger
) : LaunchApplicationHandlerApi {

    override suspend fun launchApplication() {
        if (activityFinder.currentActivity() == null) {
            val config = sdkContext.config as? AndroidEmarsysConfig
            val launchIntent = if (config?.launchActivityClass != null) {
                Intent(applicationContext, config.launchActivityClass)
            } else {
                applicationContext.packageManager.getLaunchIntentForPackage(applicationContext.packageName)
            }

            launchIntent?.let {
                val activityOptions = if (isUpsideDownCakeOrAbove) {
                    ActivityOptions.makeBasic().apply {
                        if (isUpsideDownCake) {
                            this.setPendingIntentBackgroundActivityStartMode(ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED)
                        } else  {
                            this.setPendingIntentCreatorBackgroundActivityStartMode(ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED)
                        }
                    }.toBundle()
                } else null

                PendingIntent.getActivity(
                    applicationContext,
                    0,
                    launchIntent,
                    FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE,
                    activityOptions
                ).send()
            } ?: sdkLogger.debug("LaunchApplicationHandler", "Could not create LaunchIntent")
        }
    }
}
