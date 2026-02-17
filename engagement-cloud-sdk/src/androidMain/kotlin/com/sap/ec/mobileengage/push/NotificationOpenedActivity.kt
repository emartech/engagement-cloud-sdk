package com.sap.ec.mobileengage.push

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sap.ec.di.SdkComponent
import org.koin.core.component.inject

class NotificationOpenedActivity : AppCompatActivity(), SdkComponent {
    private val notificationIntentProcessor: NotificationIntentProcessor by inject()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationIntentProcessor.processIntent(intent)
        finish()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        notificationIntentProcessor.processIntent(intent)
        finish()
    }
}