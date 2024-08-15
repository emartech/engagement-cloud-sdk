package com.emarsys.watchdog.activity

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import com.emarsys.applicationContext


class TransitionSafeCurrentActivityWatchdog : ActivityLifecycleCallbacks {

    var currentActivity: Activity? = null
        private set


    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        currentActivity = null
    }

    override fun onActivityStarted(activity: Activity) {
        currentActivity = null
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {
        if (currentActivity === activity) {
            currentActivity = null
        }
    }

    override fun onActivityStopped(activity: Activity) {
        if (activity == currentActivity) {
            currentActivity = null
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        if (activity == currentActivity) {
            currentActivity = null
        }
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (activity == currentActivity) {
            currentActivity = null
        }
    }

    fun register() {
        (applicationContext as Application).registerActivityLifecycleCallbacks(this)
    }

}
