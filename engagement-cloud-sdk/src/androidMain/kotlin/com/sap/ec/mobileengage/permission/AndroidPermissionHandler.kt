package com.sap.ec.mobileengage.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sap.ec.core.device.AndroidVersionUtils.isTiramisuOrAbove
import com.sap.ec.core.permission.PermissionHandlerApi
import com.sap.ec.watchdog.activity.TransitionSafeCurrentActivityWatchdog

class AndroidPermissionHandler(
    private val applicationContext: Context,
    private val currentActivityWatchdog: TransitionSafeCurrentActivityWatchdog,
) : PermissionHandlerApi {

    companion object {
        const val PERMISSION_REQUEST_CODE = 1234567
    }

    override suspend fun requestPushPermission() {
        if (isTiramisuOrAbove) {
            val isPushPermissionGranted = ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (isPushPermissionGranted != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    currentActivityWatchdog.waitForActivity(),
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    PERMISSION_REQUEST_CODE
                )
            }
        }
    }

}