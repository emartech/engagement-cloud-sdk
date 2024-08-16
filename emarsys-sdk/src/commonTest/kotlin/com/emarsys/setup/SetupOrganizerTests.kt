package com.emarsys.setup

import com.emarsys.EmarsysConfig
import com.emarsys.api.SdkState
import com.emarsys.context.DefaultUrls
import com.emarsys.context.SdkContext
import com.emarsys.context.SdkContextApi
import com.emarsys.core.log.LogLevel
import com.emarsys.core.state.StateMachineApi
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SetupOrganizerTests {

    private val mainDispatcher = StandardTestDispatcher()

    init {
        Dispatchers.setMain(mainDispatcher)
    }

    private lateinit var mockMeStateMachine: StateMachineApi
    private lateinit var mockPredictStateMachine: StateMachineApi
    private lateinit var sdkContext: SdkContextApi
    private lateinit var setupOrganizer: SetupOrganizerApi

    @BeforeTest
    fun setUp() {
        mockMeStateMachine = mock()
        mockPredictStateMachine = mock()
        sdkContext = SdkContext(
            StandardTestDispatcher(),
            mainDispatcher,
            DefaultUrls("", "", "", "", "", "", ""),
            LogLevel.Error,
            mutableSetOf()
        )
        setupOrganizer = SetupOrganizer(mockMeStateMachine, mockPredictStateMachine, sdkContext)
    }

    @Test
    fun setup_should_call_activate_onMeStateMachine_and_set_config_and_state_on_context() =
        runTest {
            val config = EmarsysConfig("testAppCode")
            everySuspend { mockMeStateMachine.activate() } returns Unit

            setupOrganizer.setup(config)

            verifySuspend {
                mockMeStateMachine.activate()
            }
            sdkContext.config shouldBe config
            sdkContext.currentSdkState shouldBe SdkState.active
        }

    @Test
    fun setup_should_call_activate_onPredictStateMachine_and_set_config_and_state_on_context() =
        runTest {
            val config = EmarsysConfig(null, "testMerchantId")
            everySuspend { mockPredictStateMachine.activate() } returns Unit

            setupOrganizer.setup(config)

            verifySuspend {
                mockPredictStateMachine.activate()
            }
            verifySuspend(VerifyMode.exactly(0)) {
                mockMeStateMachine.activate()
            }
            sdkContext.config shouldBe config
            sdkContext.currentSdkState shouldBe SdkState.active
        }
}