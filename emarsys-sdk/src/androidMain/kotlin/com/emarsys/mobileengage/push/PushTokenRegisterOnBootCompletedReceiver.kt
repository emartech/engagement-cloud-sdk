package com.emarsys.mobileengage.push

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class PushTokenRegisterOnBootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action.equals(Intent.ACTION_BOOT_COMPLETED, true)) {
            Log.i(
                "PushTokenRegisterOnBootCompletedReceiver",
                "Boot completed, Emarsys SDK has been started!"
            )
        }
    }
}