package com.emarsys.setup

import com.emarsys.EmarsysConfig
import com.emarsys.api.SdkState
import com.emarsys.context.SdkContext
import com.emarsys.context.SdkContextApi
import com.emarsys.context.DefaultUrls
import com.emarsys.core.log.LogLevel
import com.emarsys.core.state.StateMachineApi
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.Test

class SetupOrganizerTests : TestsWithMocks() {
    override fun setUpMocks() = injectMocks(mocker)

    @Mock
    lateinit var mockStateMachine: StateMachineApi

    private lateinit var sdkContext: SdkContextApi

    private val setupOrganizer: SetupOrganizerApi by withMocks {
        sdkContext = SdkContext(StandardTestDispatcher(), DefaultUrls("", "", "", "", "", "", ""), LogLevel.error, mutableSetOf())
        SetupOrganizer(mockStateMachine, sdkContext)
    }

    @Test
    fun setup_should_call_activate_and_set_config_and_state_on_context() = runTest {
        val config = EmarsysConfig("testAppCode")
        everySuspending { mockStateMachine.activate() } returns Unit

        setupOrganizer.setup(config)

        verifyWithSuspend {
            mockStateMachine.activate()
        }
        sdkContext.config shouldBe config
        sdkContext.currentSdkState shouldBe SdkState.active
    }
}