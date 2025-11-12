package com.emarsys.disable

import com.emarsys.api.SdkState
import com.emarsys.context.SdkContextApi
import com.emarsys.core.log.Logger
import com.emarsys.core.state.StateMachineApi
import com.emarsys.mobileengage.session.SessionApi
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.assertions.throwables.shouldThrow
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
        mockMEStateMachine = mock {
            everySuspend { activate() } returns Result.success(Unit)
        }
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
    fun testDisable_shouldActivate_MEDisableStateMachine_andEndSession() = runTest {
        disableOrganizer.disable()

        verifySuspend { mockSdkContext.setSdkState(SdkState.Inactive) }
        verifySuspend { mockMEStateMachine.activate() }
        verifySuspend { mockSession.endSession() }
    }

    @Test
    fun testDisable_shouldThrowException_whenStateMachineActivation_fails() = runTest {
        everySuspend { mockMEStateMachine.activate() } returns Result.failure(RuntimeException("test exception"))

        shouldThrow<RuntimeException> { disableOrganizer.disable() }

        verifySuspend { mockSdkContext.setSdkState(SdkState.Inactive) }
        verifySuspend { mockMEStateMachine.activate() }
        verifySuspend(VerifyMode.exactly(0)) { mockSession.endSession() }
    }
}