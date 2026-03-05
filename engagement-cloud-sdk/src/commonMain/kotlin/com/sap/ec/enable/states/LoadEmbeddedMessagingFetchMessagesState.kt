package com.sap.ec.enable.states

import com.sap.ec.context.Features
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.channel.SdkEventEmitterApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.state.State
import com.sap.ec.event.SdkEvent
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class LoadEmbeddedMessagingFetchMessagesState(
    private val sdkEventEmitter: SdkEventEmitterApi,
    private val sdkContext: SdkContextApi,
    private val sdkLogger: Logger
) : State {
    override val name: String = "loadEmbeddedMessagingFetchMessagesState"

    override fun prepare() {}

    override suspend fun active(): Result<Unit> {
        if (sdkContext.features.contains(Features.EmbeddedMessaging)) {
            sdkLogger.debug("Triggering Embedded Messaging refresh")
            sdkEventEmitter.emitEvent(SdkEvent.Internal.EmbeddedMessaging.TriggerRefresh())
        } else {
            sdkLogger.debug("Feature Embedded Messaging is disabled, skipping refresh trigger")
        }
        return Result.success(Unit)
    }

    override fun relax() {}
}
