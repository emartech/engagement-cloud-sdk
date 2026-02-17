package com.sap.ec.mobileengage.push

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sap.ec.api.push.PushApi
import com.sap.ec.api.push.PushConstants
import com.sap.ec.core.extension.goAsync
import com.sap.ec.core.log.Logger
import com.sap.ec.core.storage.StringStorageApi
import com.sap.ec.di.DispatcherTypes
import com.sap.ec.di.SdkComponent
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
        intent.getStringExtra(PushConstants.PUSH_TOKEN_KEY)?.let {
            logger.debug("push token received: token: $it")
            stringStorage.put(PushConstants.PUSH_TOKEN_STORAGE_KEY, it)
            pushApi.registerPushToken(it)
        }
    }
}
