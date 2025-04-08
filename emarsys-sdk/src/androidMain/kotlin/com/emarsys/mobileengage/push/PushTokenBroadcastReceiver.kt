package com.emarsys.mobileengage.push

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.emarsys.api.push.PushApi
import com.emarsys.api.push.PushConstants
import com.emarsys.core.extension.goAsync
import com.emarsys.core.log.Logger
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.di.DispatcherTypes
import com.emarsys.di.SdkComponent
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named

internal class PushTokenBroadcastReceiver : BroadcastReceiver(), SdkComponent {
    private val sdkDispatcher: CoroutineDispatcher = get(named(DispatcherTypes.Sdk))
    private val stringStorage: StringStorageApi = get()
    private val pushApi: PushApi = get()
    private val logger: Logger = get { parametersOf(PushTokenBroadcastReceiver::class.simpleName) }

    override fun onReceive(context: Context, intent: Intent) = goAsync(sdkDispatcher) {
        intent.getStringExtra(PushConstants.PUSH_TOKEN_INTENT_KEY)?.let {
                logger.debug("push token received: token: $it")
                stringStorage.put(PushConstants.PUSH_TOKEN_STORAGE_KEY, it)
                pushApi.registerPushToken(it)
            }
    }
}
