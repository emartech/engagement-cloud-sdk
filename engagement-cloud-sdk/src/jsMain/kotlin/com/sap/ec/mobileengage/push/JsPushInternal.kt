package com.sap.ec.mobileengage.push

import com.sap.ec.api.push.PushCall
import com.sap.ec.api.push.PushCall.ClearPushToken
import com.sap.ec.api.push.PushCall.RegisterPushToken
import com.sap.ec.api.push.PushContextApi
import com.sap.ec.api.push.PushInternal
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.collections.dequeue
import com.sap.ec.core.device.notification.PermissionState
import com.sap.ec.core.log.Logger
import com.sap.ec.core.storage.StringStorageApi
import com.sap.ec.util.runCatchingWithoutCancellation

internal class JsPushInternal(
    storage: StringStorageApi,
    private val pushContext: PushContextApi,
    sdkContext: SdkContextApi,
    sdkEventDistributor: SdkEventDistributorApi,
    private val sdkLogger: Logger,
    private val pushService: PushServiceApi,
) : PushInternal(storage, pushContext, sdkEventDistributor, sdkContext, sdkLogger),
    JsPushInstance {

    override suspend fun subscribe(): Result<Unit> {
        return pushService.subscribe().mapCatching {
            it?.let {
                registerPushToken(it)
            }
        }
    }

    override suspend fun unsubscribe(): Result<Unit> {
        return runCatchingWithoutCancellation {
            clearPushToken()
            pushService.unsubscribe().getOrThrow()
        }
    }

    override suspend fun isSubscribed(): Boolean {
        return getPushToken() != null
    }

    override suspend fun getPermissionState(): PermissionState {
        return pushService.getPermissionState()
    }

    override suspend fun activate() {
        sdkLogger.debug("JsPushInternal - activate")
        pushContext.calls.dequeue { call ->
            when (call) {
                PushCall.Subscribe -> subscribe()
                PushCall.Unsubscribe -> unsubscribe()
                is ClearPushToken,
                is PushCall.HandleSilentMessageWithUserInfo,
                is RegisterPushToken -> {
                }
            }
        }
    }
}

