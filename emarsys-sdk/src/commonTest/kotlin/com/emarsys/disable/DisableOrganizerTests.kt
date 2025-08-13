package com.emarsys.disable

import com.emarsys.api.SdkState
import com.emarsys.context.SdkContextApi
import com.emarsys.core.log.Logger
import com.emarsys.core.state.StateMachineApi
import com.emarsys.mobileengage.session.SessionApi
import dev.mokkery.MockMode
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class DisableOrganizerTests {

    private lateinit var disableOrganizer: DisableOrganizer
    private lateinit var mockMEStateMachine: StateMachineApi
    private lateinit var mockSession: SessionApi
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var mockSdkLogger: Logger

    @BeforeTest
    fun setup() {
        mockMEStateMachine = mock(MockMode.autofill)
        mockSession = mock(MockMode.autofill)
        mockSdkContext = mock(MockMode.autofill)
        mockSdkLogger = mock(MockMode.autofill)

        disableOrganizer = DisableOrganizer(
            mobileEngageDisableStateMachine = mockMEStateMachine,
            sdkContext = mockSdkContext,
            mockSession,
            sdkLogger = mockSdkLogger
        )
    }

    @Test
    fun testDisable_shouldActivate_MEOrganizer() = runTest {

        disableOrganizer.disable()

        verifySuspend { mockSdkContext.setSdkState(SdkState.inactive) }
        verifySuspend { mockMEStateMachine.activate() }
        verifySuspend { mockSession.endSession() }
    }
}