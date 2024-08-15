package com.emarsys.setup

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import com.emarsys.core.state.State

class PlatformInitState(
    private val pushTokenBroadcastReceiver: BroadcastReceiver,
    private val pushTokenIntentFilter: IntentFilter,
    private val pushMessageBroadcastReceiver: BroadcastReceiver,
    private val pushMessageIntentFilter: IntentFilter,
    private val applicationContext: Context
) : State {

    override val name: String = "androidInitState"

    override fun prepare() {
        ContextCompat.registerReceiver(
            applicationContext,
            pushTokenBroadcastReceiver,
            pushTokenIntentFilter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        ContextCompat.registerReceiver(
            applicationContext,
            pushMessageBroadcastReceiver,
            pushMessageIntentFilter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override suspend fun active() {
    }

    override fun relax() {
    }
}