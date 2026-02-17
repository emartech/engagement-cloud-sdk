package com.sap.ec.watchdog.activity

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import com.sap.ec.applicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference


class TransitionSafeCurrentActivityWatchdog : ActivityLifecycleCallbacks, ActivityFinderApi {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var setActivityJob: Job? = null
    private var currentActivity: WeakReference<Activity?> = WeakReference(null)
    private val currentActivityFlow: MutableStateFlow<Activity?> = MutableStateFlow(null)

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        currentActivityFlow.value = null
        currentActivity = WeakReference(null)
    }

    override fun onActivityStarted(activity: Activity) {
        currentActivityFlow.value = null
        currentActivity = WeakReference(null)
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = WeakReference(activity)
        setActivityJob = scope.launch {
            delay(500)
            currentActivity.get()?.let {
                currentActivityFlow.value = it
            }
        }
    }

    override fun onActivityPaused(activity: Activity) {
        if (activity == currentActivityFlow.value) {
            currentActivityFlow.value = null
            setActivityJob?.cancel()
            currentActivity = WeakReference(null)
        }
    }

    override fun onActivityStopped(activity: Activity) {
        if (activity == currentActivityFlow.value) {
            currentActivityFlow.value = null
            setActivityJob?.cancel()
            currentActivity = WeakReference(null)
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        if (activity == currentActivityFlow.value) {
            currentActivityFlow.value = null
            setActivityJob?.cancel()
            currentActivity = WeakReference(null)
        }
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (activity == currentActivityFlow.value) {
            currentActivityFlow.value = null
            setActivityJob?.cancel()
            currentActivity = WeakReference(null)
        }
    }

    fun register() {
        (applicationContext as Application).registerActivityLifecycleCallbacks(this)
    }

    override suspend fun waitForActivity(): Activity {
        return currentActivityFlow.first { activity -> activity != null}!!
    }

    override suspend fun currentActivity(): Activity? {
        return currentActivityFlow.first()
    }
}
