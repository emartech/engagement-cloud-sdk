package com.emarsys.setup

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import com.emarsys.core.state.State

class PlatformInitState(
    private val pushTokenBroadcastReceiver: BroadcastReceiver,
    private val intentFilter: IntentFilter,
    private val context: Context
) : State {

    override val name: String = "androidInitState"

    override fun prepare() {
        ContextCompat.registerReceiver(
            context,
            pushTokenBroadcastReceiver,
            intentFilter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override suspend fun active() {
    }

    override fun relax() {
    }
}