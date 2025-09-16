package com.emarsys.enable.states

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.providers.InstantProvider
import com.emarsys.core.providers.UuidProviderApi
import com.emarsys.core.state.State
import com.emarsys.event.SdkEvent
import com.emarsys.response.mapToUnitOrFailure
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