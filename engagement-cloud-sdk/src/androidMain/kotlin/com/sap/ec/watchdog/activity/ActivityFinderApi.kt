package com.sap.ec.watchdog.activity

import android.app.Activity

interface ActivityFinderApi {

    suspend fun waitForActivity(): Activity

    suspend fun currentActivity(): Activity?

}
