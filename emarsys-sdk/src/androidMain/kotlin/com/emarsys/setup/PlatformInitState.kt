package com.emarsys.setup

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import com.emarsys.api.push.PushConstants.PUSH_TOKEN_INTENT_FILTER_ACTION
import com.emarsys.setup.states.PlatformInitStateApi

class PlatformInitState(
    private val pushTokenBroadcastReceiver: BroadcastReceiver,
    private val context: Context
) : PlatformInitStateApi {

    override val name: String = "androidInitState"

    override fun prepare() {
        ContextCompat.registerReceiver(
            context,
            pushTokenBroadcastReceiver,
            IntentFilter(PUSH_TOKEN_INTENT_FILTER_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override suspend fun active() {
    }

    override fun relax() {
    }
}