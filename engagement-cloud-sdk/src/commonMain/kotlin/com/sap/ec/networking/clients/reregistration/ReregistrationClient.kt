package com.sap.ec.networking.clients.reregistration

import com.sap.ec.api.SdkState
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.channel.SdkEventManagerApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.state.StateMachineApi
import com.sap.ec.event.SdkEvent
import com.sap.ec.networking.clients.EventBasedClientApi
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
                sdkContext.setSdkState(SdkState.OnHold)  // TODO use operational events
                mobileEngageReregistrationStateMachine.activate()
                    .onSuccess {
                        sdkContext.setSdkState(SdkState.Active)
                        sdkLogger.debug("Reregistration finished")
                    }.onFailure {
                        sdkLogger.error("Error during re-registration", it)
                    }
            }.catch {
                sdkLogger.error("Error in re-registration flow collection", it)
            }.collect()
    }

}