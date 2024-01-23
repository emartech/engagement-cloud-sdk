package com.emarsys.sample

import android.app.Application
import com.emarsys.Emarsys
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.Default).launch {
            Emarsys.initialize()
        }
    }
}