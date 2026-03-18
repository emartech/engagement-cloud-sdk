package com.sap.ec.di

import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.channel.SdkEventEmitterApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.state.State
import com.sap.ec.core.state.StateMachine
import com.sap.ec.core.state.TestState
import com.sap.ec.event.SdkEvent
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

internal class FollowUpChangeAppCodeStateMachineTests {
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var mockSdkEventEmitter: SdkEventEmitterApi
    private lateinit var mockLogger: Logger

    @BeforeTest
    fun setup() {
        mockSdkContext = mock(MockMode.autofill)
        mockSdkEventEmitter = mock(MockMode.autofill)
        mockLogger = mock(MockMode.autofill)
    }

    private fun createStateMachineWithRealLoadState(): StateMachine {
        val applyConfigState = mock<State>(MockMode.autofill) {
            every { name } returns "applyAppCodeBasedRemoteConfig"
            everySuspend { active() } returns Result.success(Unit)
        }
        val fetchMetaState = mock<State>(MockMode.autofill) {
            every { name } returns "fetchEmbeddedMessagingMetaState"
            everySuspend { active() } returns Result.success(Unit)
        }
        return StateMachine(
            states = listOf(applyConfigState, fetchMetaState),
            name = StateMachineTypes.FollowUpChangeAppCodeStateMachine.name,
            logger = mockLogger
        )
    }

    @Test
    fun activate_shouldNotEmitTriggerRefresh_whenEmbeddedMessagingDisabled() = runTest {
        every { mockSdkContext.features } returns mutableSetOf()

        val stateMachine = createStateMachineWithRealLoadState()
        val result = stateMachine.activate()

        result shouldBe Result.success(Unit)
        verifySuspend(VerifyMode.exactly(0)) {
            mockSdkEventEmitter.emitEvent(any<SdkEvent.Internal.EmbeddedMessaging.TriggerRefresh>())
        }
    }

    @Test
    fun activate_shouldActivateAllThreeStates_inCorrectOrder() = runTest {
        val applyConfig = TestState("applyAppCodeBasedRemoteConfig")
        val fetchMeta = TestState("fetchEmbeddedMessagingMetaState")

        val activatedStateNames = mutableListOf<String>()
        listOf(applyConfig, fetchMeta).forEach { state ->
            state.functionCalls = { stateName, functionName ->
                if (functionName == "active") {
                    activatedStateNames.add(stateName)
                }
            }
        }

        val stateMachine = StateMachine(
            states = listOf(applyConfig, fetchMeta),
            name = StateMachineTypes.FollowUpChangeAppCodeStateMachine.name,
            logger = mockLogger
        )

        stateMachine.activate()

        activatedStateNames shouldBe listOf(
            "applyAppCodeBasedRemoteConfig",
            "fetchEmbeddedMessagingMetaState",
        )
    }
}
