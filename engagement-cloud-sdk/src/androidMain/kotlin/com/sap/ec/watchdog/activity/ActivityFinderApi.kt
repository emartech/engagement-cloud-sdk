package com.sap.ec.watchdog.activity

import android.app.Activity

internal interface ActivityFinderApi {

    suspend fun waitForActivity(): Activity

    suspend fun currentActivity(): Activity?

}
