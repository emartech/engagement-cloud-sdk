package com.sap.ec.sample


import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.sap.ec.EngagementCloud
import com.sap.ec.api.event.model.AppEvent
import com.sap.ec.api.event.model.BadgeCountEvent
import com.sap.ec.api.event.model.EventType
import com.sap.ec.core.device.AndroidVersionUtils.isTiramisuOrAbove
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
        lifecycleScope.launch {
            askNotificationPermission()

            EngagementCloud.events.collect {
                when (it.type) {
                    EventType.APP_EVENT -> {
                        with(it as AppEvent) {
                            Log.i("Engagement Cloud SDK", "Received App Event: $name with payload: $payload")
                        }
                    }
                    EventType.BADGE_COUNT -> {
                        with(it as BadgeCountEvent) {
                            Log.i("Engagement Cloud SDK", "Received Badge Count Event: $badgeCount for method: $method")
                        }
                    }
                }
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.i("Engagement Cloud SDK", "Permission Granted")
        } else {
            Log.i("Engagement Cloud SDK", "Permission Denied")
        }
    }

    private fun askNotificationPermission() {
        if (isTiramisuOrAbove) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}