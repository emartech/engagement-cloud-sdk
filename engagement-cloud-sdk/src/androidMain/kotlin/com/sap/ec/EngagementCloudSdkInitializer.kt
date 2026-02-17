package com.sap.ec

import android.content.Context
import androidx.startup.Initializer
import kotlinx.coroutines.runBlocking

internal lateinit var applicationContext: Context
    private set

internal class EngagementCloudSdkInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        setContext(context)
        return runBlocking {
            AndroidEngagementCloud.initialize()
        }
    }

    private fun setContext(context: Context) {
        applicationContext = context.applicationContext
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}