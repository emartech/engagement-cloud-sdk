package com.emarsys.mobileengage.action.actions

import com.emarsys.core.log.SdkLogger

class PushToInappAction(private val sdkLogger: SdkLogger) : Action<Unit> {
    override suspend fun invoke(value: Unit?) {
        sdkLogger.info("pushToInappAction", "PushToInappAction received as DefaultTapAction")
    }
}