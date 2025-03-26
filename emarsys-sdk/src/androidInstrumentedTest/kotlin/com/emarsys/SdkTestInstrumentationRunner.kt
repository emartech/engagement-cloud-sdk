package com.emarsys

import android.app.Application
import androidx.test.runner.AndroidJUnitRunner

class SdkTestInstrumentationRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: android.content.Context?
    ): Application? {
        return super.newApplication(cl, TestApplication::class.java.name, context)
    }
}