package com.emarsys.mobileengage.push

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.emarsys.di.AndroidPlatformContext
import com.emarsys.di.DependencyContainerPrivateApi
import com.emarsys.di.DependencyInjection

class NotificationOpenedActivity(
    private val notificationIntentProcessor: NotificationIntentProcessor
) : AppCompatActivity() {

    constructor() : this(
        ((DependencyInjection.container as DependencyContainerPrivateApi).platformContext as AndroidPlatformContext).notificationIntentProcessor
    )

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationIntentProcessor.processIntent(intent, lifecycleScope)
        finish()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        notificationIntentProcessor.processIntent(intent, lifecycleScope)
        finish()
    }
}