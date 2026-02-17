package com.sap.ec.init

import com.sap.ec.api.SdkState
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.state.StateMachineApi
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.answering.sequentiallyReturns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class InitOrganizerTests {
    private lateinit var testDispatcher: TestDispatcher
    private lateinit var mockStateMachine: StateMachineApi
    private lateinit var mockLogger: Logger
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var initOrganizer: InitOrganizer

    @BeforeTest
    fun setUp() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)

        mockStateMachine = mock(MockMode.autofill)
        mockSdkContext = mock(MockMode.autofill)
        everySuspend { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.UnInitialized)

        mockLogger = mock(MockMode.autofill)
        initOrganizer = InitOrganizer(
            mockStateMachine,
            mockSdkContext,
            mockLogger
        )
    }

    @Test
    fun init_should_call_activate_onStateMachine_and_set_config_and_state_on_context() =
        runTest {
            everySuspend { mockStateMachine.activate() } returns Result.success(Unit)

            initOrganizer.init()

            verifySuspend {
                mockStateMachine.activate()
            }
            verifySuspend { mockSdkContext.setSdkState(SdkState.Initialized) }
        }

    @Test
    fun init_should_only_call_activate_onStateMachine_when_the_sdkState_is_unInitialized() =
        runTest {
            everySuspend { mockStateMachine.activate() } returns Result.success(Unit)

            initOrganizer.init()

            verifySuspend { mockSdkContext.setSdkState(SdkState.Initialized) }

            everySuspend { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Initialized)

            initOrganizer.init()

            verifySuspend(VerifyMode.exactly(1)) { mockStateMachine.activate() }
        }

    @Test
    fun init_should_not_move_sdkState_backwards_whenInitStateMachine_alreadyActivatedTheSDK() =
        runTest {
            everySuspend { mockStateMachine.activate() } returns Result.success(Unit)
            everySuspend { mockSdkContext.currentSdkState } sequentiallyReturns listOf(
                MutableStateFlow(SdkState.UnInitialized),
                MutableStateFlow(SdkState.OnHold)
            )

            initOrganizer.init()

            verifySuspend(VerifyMode.exactly(1)) { mockStateMachine.activate() }
            verifySuspend(VerifyMode.exactly(0)) { mockSdkContext.setSdkState(SdkState.UnInitialized) }
            verifySuspend(VerifyMode.exactly(0)) { mockSdkContext.setSdkState(SdkState.OnHold) }
            verifySuspend(VerifyMode.exactly(0)) { mockSdkContext.setSdkState(SdkState.Initialized) }
        }

    @Test
    fun init_should_throwException_ifStateMachineActivation_throws() =
        runTest {
            val testException = Exception("failure")
            everySuspend { mockStateMachine.activate() } returns Result.failure(testException)

            val exception = shouldThrow<Exception> { initOrganizer.init() }

            verifySuspend { mockStateMachine.activate() }
            verifySuspend(VerifyMode.exactly(0)) { mockSdkContext.setSdkState(SdkState.Active) }
            exception shouldBe testException
            verifySuspend { mockLogger.debug("SDK initialization failed.", testException) }
        }
}
