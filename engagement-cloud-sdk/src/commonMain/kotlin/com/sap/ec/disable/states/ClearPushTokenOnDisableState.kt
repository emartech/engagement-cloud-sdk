package com.sap.ec.disable.states

import com.sap.ec.api.push.PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.networking.model.Response
import com.sap.ec.core.state.State
import com.sap.ec.core.storage.StringStorageApi
import com.sap.ec.event.SdkEvent
import com.sap.ec.response.mapToUnitOrFailure
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)

internal class ClearPushTokenOnDisableState(
    private val sdkEventDistributor: SdkEventDistributorApi,
    private val storage: StringStorageApi,
    private val sdkContext: SdkContextApi
) : State {
    override val name: String = "clearPushTokenOnDisableState"

    override fun prepare() {
    }

    override suspend fun active(): Result<Unit> {
        if (storage.get(LAST_SENT_PUSH_TOKEN_STORAGE_KEY) == null) {
            return Result.success(Unit)
        }
        return sdkEventDistributor.registerEvent(
            SdkEvent.Internal.Sdk.ClearPushToken(applicationCode = sdkContext.getSdkConfig()?.applicationCode)
        ).await<Response>()
            .mapToUnitOrFailure()
    }

    override fun relax() {
    }
}