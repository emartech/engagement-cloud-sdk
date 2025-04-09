package com.emarsys.init

import com.emarsys.api.SdkState
import com.emarsys.context.DefaultUrls
import com.emarsys.context.SdkContext
import com.emarsys.context.SdkContextApi
import com.emarsys.core.log.ConsoleLogger
import com.emarsys.core.log.LogLevel
import com.emarsys.core.log.SdkLogger
import com.emarsys.core.state.StateMachineApi
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class InitOrganizerTests {

    private val mainDispatcher = StandardTestDispatcher()

    init {
        Dispatchers.setMain(mainDispatcher)
    }

    private lateinit var mockStateMachine: StateMachineApi
    private lateinit var mockPredictStateMachine: StateMachineApi
    private lateinit var sdkContext: SdkContextApi
    private lateinit var initOrganizer: InitOrganizer

    @BeforeTest
    fun setUp() {
        mockStateMachine = mock()
        mockPredictStateMachine = mock()
        sdkContext = SdkContext(
            StandardTestDispatcher(),
            mainDispatcher,
            DefaultUrls("", "", "", "", "", "", ""),
            LogLevel.Error,
            mutableSetOf(),
            logBreadcrumbsQueueSize = 10
        )
        initOrganizer = InitOrganizer(mockStateMachine, sdkContext, SdkLogger("TestLoggerName", ConsoleLogger(), sdkContext = mock()))
    }

    @Test
    fun init_should_call_activate_onStateMachine_and_set_config_and_state_on_context() =
        runTest {
            everySuspend { mockStateMachine.activate() } returns Unit

            initOrganizer.init()

            verifySuspend {
                mockStateMachine.activate()
            }
            sdkContext.currentSdkState.value shouldBe SdkState.initialized
        }

    @Test
    fun init_should_not_move_sdkState_backwards_whenInitStateMachine_alreadyActivatedTheSDK() =
        runTest {
            everySuspend { mockStateMachine.activate() } calls {
                sdkContext.setSdkState(SdkState.active)
            }

            initOrganizer.init()

            verifySuspend {
                mockStateMachine.activate()
            }
            sdkContext.currentSdkState.value shouldBe SdkState.active
        }
}
