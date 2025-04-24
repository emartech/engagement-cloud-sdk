package com.emarsys.disable

import com.emarsys.api.SdkState
import com.emarsys.context.SdkContextApi
import com.emarsys.core.log.Logger
import com.emarsys.core.state.StateMachineApi
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class DisableOrganizerTests {

    private lateinit var disableOrganizer: DisableOrganizer
    private lateinit var mockMEStateMachine: StateMachineApi
    private lateinit var mockPredictStateMachine: StateMachineApi
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var mockSdkLogger: Logger

    @BeforeTest
    fun setup() {
        mockMEStateMachine = mock(MockMode.autofill)
        mockPredictStateMachine = mock(MockMode.autofill)
        mockSdkContext = mock(MockMode.autofill)
        mockSdkLogger = mock(MockMode.autofill)

        disableOrganizer = DisableOrganizer(
            mobileEngageDisableStateMachine = mockMEStateMachine,
            predictDisableStateMachine = mockPredictStateMachine,
            sdkContext = mockSdkContext,
            sdkLogger = mockSdkLogger
        )
    }

    @Test
    fun testDisable_shouldActivate_MEOrganizer() = runTest {
        everySuspend { mockSdkContext.isConfigPredictOnly() } returns false

        disableOrganizer.disable()

        verifySuspend { mockSdkContext.setSdkState(SdkState.inactive) }
        verifySuspend { mockMEStateMachine.activate() }
    }

    @Test
    fun testDisable_shouldActivate_PredictOrganizer() = runTest {
        everySuspend { mockSdkContext.isConfigPredictOnly() } returns true

        disableOrganizer.disable()

        verifySuspend { mockSdkContext.setSdkState(SdkState.inactive) }
        verifySuspend { mockPredictStateMachine.activate() }
    }
}