package com.sap.ec.enable.states

import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.networking.model.Response
import com.sap.ec.core.providers.InstantProvider
import com.sap.ec.core.providers.UuidProviderApi
import com.sap.ec.core.state.State
import com.sap.ec.event.SdkEvent
import com.sap.ec.response.mapToUnitOrFailure
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class AppStartState(
    private val sdkEventDistributor: SdkEventDistributorApi,
    private val timestampProvider: InstantProvider,
    private val uuidProvider: UuidProviderApi
) : State {
    private var alreadyCompleted = false

    override val name: String = "appStartState"
    override fun prepare() {
    }

    override suspend fun active(): Result<Unit> {
        return if (!alreadyCompleted) {
            val appStartEvent = SdkEvent.Internal.Sdk.AppStart(
                id = uuidProvider.provide(),
                timestamp = timestampProvider.provide()
            )
            sdkEventDistributor.registerEvent(appStartEvent)
                .await<Response>()
                .mapToUnitOrFailure()
                .onSuccess {
                    alreadyCompleted = true
                }
        } else {
            Result.success(Unit)
        }
    }

    override fun relax() {
    }
}