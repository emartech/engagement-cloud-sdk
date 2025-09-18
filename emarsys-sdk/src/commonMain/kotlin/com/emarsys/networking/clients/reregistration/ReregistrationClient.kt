package com.emarsys.networking.clients.reregistration

import com.emarsys.api.SdkState
import com.emarsys.context.SdkContextApi
import com.emarsys.core.channel.SdkEventManagerApi
import com.emarsys.core.log.Logger
import com.emarsys.core.state.StateMachineApi
import com.emarsys.event.SdkEvent
import com.emarsys.networking.clients.EventBasedClientApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

internal class ReregistrationClient(
    private val sdkEventManager: SdkEventManagerApi,
    private val sdkContext: SdkContextApi,
    private val mobileEngageReregistrationStateMachine: StateMachineApi,
    private val applicationScope: CoroutineScope,
    private val sdkLogger: Logger
) : EventBasedClientApi {

    override suspend fun register() {
        applicationScope.launch(start = CoroutineStart.UNDISPATCHED) {
            startEventConsumer()
        }
    }

    private suspend fun startEventConsumer() {
        sdkEventManager.sdkEventFlow
            .filterIsInstance<SdkEvent.Internal.Sdk.ReregistrationRequired>()
            .onEach {
                sdkLogger.debug("Reregistration start")
                sdkContext.setSdkState(SdkState.onHold)
                mobileEngageReregistrationStateMachine.activate()
                    .onSuccess {
                        sdkContext.setSdkState(SdkState.active)
                        sdkLogger.debug("Reregistration finished")
                    }.onFailure {
                        sdkLogger.error("Error during re-registration", it)
                    }
            }.catch {
                sdkLogger.error("Error in re-registration flow collection", it)
            }.collect()
    }

}