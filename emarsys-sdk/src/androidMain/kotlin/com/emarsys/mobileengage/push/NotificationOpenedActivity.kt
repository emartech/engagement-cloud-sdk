package com.emarsys.mobileengage.push

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.emarsys.di.SdkComponent
import org.koin.core.component.inject

class NotificationOpenedActivity() : AppCompatActivity(), SdkComponent {
    private val notificationIntentProcessor: NotificationIntentProcessor by inject()

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