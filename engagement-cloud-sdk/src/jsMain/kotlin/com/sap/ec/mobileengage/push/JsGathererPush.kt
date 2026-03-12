package com.sap.ec.mobileengage.push

import com.sap.ec.api.push.PushCall
import com.sap.ec.api.push.PushContextApi
import com.sap.ec.core.device.notification.PermissionState
import com.sap.ec.core.log.Logger

internal class JsGathererPush(
    private val context: PushContextApi,
    private val jsPushInternal: JsPushInstance,
    private val sdkLogger: Logger
) : JsPushInstance {

    override suspend fun subscribe(): Result<Unit> {
        sdkLogger.debug("JsGathererPush: subscribe called")
        context.calls.add(PushCall.Subscribe)
        return Result.success(Unit)
    }

    override suspend fun unsubscribe(): Result<Unit> {
        sdkLogger.debug("JsGathererPush: unsubscribe called")
        context.calls.add(PushCall.Unsubscribe)
        return Result.success(Unit)
    }

    override suspend fun isSubscribed(): Boolean {
        sdkLogger.debug("JsGathererPush: isSubscribed called")
        return jsPushInternal.isSubscribed()
    }

    override suspend fun getPermissionState(): PermissionState {
        return jsPushInternal.getPermissionState()
    }

    override suspend fun activate() {

    }
}

