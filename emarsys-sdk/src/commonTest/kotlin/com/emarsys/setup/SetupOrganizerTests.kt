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
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class SetupOrganizerTests {
    private lateinit var mockStateMachine: StateMachineApi
    private lateinit var sdkContext: SdkContextApi
    private lateinit var setupOrganizer: SetupOrganizerApi

    @BeforeTest
    fun setUp() {
        mockStateMachine = mock()
        sdkContext = SdkContext(
            StandardTestDispatcher(),
            DefaultUrls("", "", "", "", "", "", ""),
            LogLevel.Error,
            mutableSetOf()
        )
        setupOrganizer = SetupOrganizer(mockStateMachine, sdkContext)
    }

    @Test
    fun setup_should_call_activate_and_set_config_and_state_on_context() = runTest {
        val config = EmarsysConfig("testAppCode")
        everySuspend { mockStateMachine.activate() } returns Unit

        setupOrganizer.setup(config)

        verifySuspend {
            mockStateMachine.activate()
        }
        sdkContext.config shouldBe config
        sdkContext.currentSdkState shouldBe SdkState.active
    }
}