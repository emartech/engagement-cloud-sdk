package com.emarsys

import android.content.Context
import androidx.startup.Initializer
import kotlinx.coroutines.runBlocking

internal lateinit var applicationContext: Context
private set

class EmarsysSdkInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        applicationContext = context.applicationContext
        return runBlocking {
            Emarsys.initialize()
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}