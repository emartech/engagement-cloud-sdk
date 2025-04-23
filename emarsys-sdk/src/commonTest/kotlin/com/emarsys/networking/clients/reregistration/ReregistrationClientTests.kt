package com.emarsys.networking.clients.reregistration

import com.emarsys.api.SdkState
import com.emarsys.context.SdkContextApi
import com.emarsys.core.channel.SdkEventManagerApi
import com.emarsys.core.log.Logger
import com.emarsys.core.state.StateMachineApi
import com.emarsys.networking.clients.event.model.SdkEvent
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class ReregistrationClientTests {

    private lateinit var sdkEventFlow: MutableSharedFlow<SdkEvent>
    private lateinit var mockSdkEventManager: SdkEventManagerApi
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var mockMobileEngageReregistrationStateMachine: StateMachineApi
    private lateinit var mockPredictOnlyReregistrationStateMachine: StateMachineApi
    private lateinit var mockSdkLogger: Logger

    private lateinit var reregistrationClient: ReregistrationClient

    @BeforeTest
    fun setup() {

        mockSdkEventManager = mock()
        mockSdkLogger = mock(MockMode.autofill)
        mockSdkContext = mock(MockMode.autofill)
        mockMobileEngageReregistrationStateMachine = mock(MockMode.autofill)
        mockPredictOnlyReregistrationStateMachine = mock(MockMode.autofill)

        sdkEventFlow = MutableSharedFlow()
        every { mockSdkEventManager.sdkEventFlow } returns sdkEventFlow
    }

    @Test
    fun testRegister_shouldSubscribeToOnlineSdkEvents() = runTest {
        reregistrationClient = createReregistrationClient(backgroundScope)
        reregistrationClient.register()

        verifySuspend { mockSdkEventManager.sdkEventFlow.filterIsInstance<SdkEvent.Internal.Sdk.ReregistrationRequired>() }
    }

    @Test
    fun testStartEventConsumer_shouldSetSdkState_toOnHold_activateMobileEngageStateMachine_andSetSdkStateToOnActive() =
        runTest {
            reregistrationClient = createReregistrationClient(backgroundScope)
            reregistrationClient.register()

            val event = SdkEvent.Internal.Sdk.ReregistrationRequired()

            sdkEventFlow.emit(event)

            verifySuspend(VerifyMode.order) {
                mockSdkContext.setSdkState(SdkState.onHold)
                mockMobileEngageReregistrationStateMachine.activate()
                mockSdkContext.setSdkState(SdkState.active)
            }
            verifySuspend(VerifyMode.exactly(0)) {
                mockPredictOnlyReregistrationStateMachine.activate()
            }
        }

    @Test
    fun testStartEventConsumer_shouldSetSdkState_toOnHold_activatePredictOnlyStateMachine_andSetSdkStateToOnActive_whenConfigIsPredictOnly() =
        runTest {
            reregistrationClient = createReregistrationClient(backgroundScope)
            reregistrationClient.register()
            every { mockSdkContext.isConfigPredictOnly() } returns true

            val event = SdkEvent.Internal.Sdk.ReregistrationRequired()

            sdkEventFlow.emit(event)

            verifySuspend(VerifyMode.order) {
                mockSdkContext.setSdkState(SdkState.onHold)
                mockPredictOnlyReregistrationStateMachine.activate()
                mockSdkContext.setSdkState(SdkState.active)
            }
            verifySuspend(VerifyMode.exactly(0)) {
                mockMobileEngageReregistrationStateMachine.activate()
            }
        }

    @Test
    fun testStartEventConsumer_shouldCatchExceptions_andLog() =
        runTest {
            reregistrationClient = createReregistrationClient(backgroundScope)
            reregistrationClient.register()
            val testException = Exception("Test exception")
            everySuspend { mockSdkContext.setSdkState(SdkState.onHold) } throws testException

            val event = SdkEvent.Internal.Sdk.ReregistrationRequired()

            sdkEventFlow.emit(event)

            verifySuspend {
                mockSdkContext.setSdkState(SdkState.onHold)
                mockSdkLogger.error("Error during re-registration", testException)
            }
            verifySuspend(VerifyMode.exactly(0)) {
                mockMobileEngageReregistrationStateMachine.activate()
                mockPredictOnlyReregistrationStateMachine.activate()
                mockSdkContext.setSdkState(SdkState.active)
            }
        }

    private fun createReregistrationClient(applicationScope: CoroutineScope): ReregistrationClient {
        return ReregistrationClient(
            sdkEventManager = mockSdkEventManager,
            sdkContext = mockSdkContext,
            mobileEngageReregistrationStateMachine = mockMobileEngageReregistrationStateMachine,
            predictOnlyReregistrationStateMachine = mockPredictOnlyReregistrationStateMachine,
            applicationScope = applicationScope,
            sdkLogger = mockSdkLogger,
        )
    }
}