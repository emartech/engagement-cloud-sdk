package com.sap.ec

import android.app.Application
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.jvm.isAccessible

class TestApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val setter = EngagementCloudSdkInitializer::class.declaredFunctions.find { it.name == "setContext" }
        println("Setting ApplicationContext, ${setter == null}")
        setter?.isAccessible = true
        setter?.call(EngagementCloudSdkInitializer(), applicationContext)
    }
}