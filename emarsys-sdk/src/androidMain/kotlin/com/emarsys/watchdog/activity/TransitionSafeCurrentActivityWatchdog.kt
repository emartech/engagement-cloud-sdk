package com.emarsys.watchdog.activity

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import com.emarsys.applicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


class TransitionSafeCurrentActivityWatchdog : ActivityLifecycleCallbacks {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var setActivityJob: Job? = null
    private val currentActivityFlow: MutableStateFlow<Activity?> = MutableStateFlow(null)

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        currentActivityFlow.value = null
    }

    override fun onActivityStarted(activity: Activity) {
        currentActivityFlow.value = null
    }

    override fun onActivityResumed(activity: Activity) {
        setActivityJob = scope.launch {
            delay(500)
            currentActivityFlow.value = activity
        }
    }

    override fun onActivityPaused(activity: Activity) {
        if (activity == currentActivityFlow.value) {
            currentActivityFlow.value = null
            setActivityJob?.cancel()
        }
    }

    override fun onActivityStopped(activity: Activity) {
        if (activity == currentActivityFlow.value) {
            currentActivityFlow.value = null
            setActivityJob?.cancel()
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        if (activity == currentActivityFlow.value) {
            currentActivityFlow.value = null
            setActivityJob?.cancel()
        }
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (activity == currentActivityFlow.value) {
            currentActivityFlow.value = null
            setActivityJob?.cancel()
        }
    }

    fun register() {
        (applicationContext as Application).registerActivityLifecycleCallbacks(this)
    }

    suspend fun getCurrentActivity(): Activity {
       return currentActivityFlow.first { activity -> activity != null}!!
    }
}
