package com.sap.ec.disable

import com.sap.ec.api.SdkState
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.exceptions.SdkException.SdkAlreadyDisabledException
import com.sap.ec.core.log.Logger
import com.sap.ec.core.state.StateMachineApi
import com.sap.ec.mobileengage.session.SessionApi
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class DisableOrganizerTests {

    private lateinit var disableOrganizer: DisableOrganizer
    private lateinit var mockMEDisableStateMachine: StateMachineApi
    private lateinit var mockSession: SessionApi
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var mockSdkLogger: Logger

    @BeforeTest
    fun setup() {
        mockMEDisableStateMachine = mock {
            everySuspend { activate() } returns Result.success(Unit)
        }
        mockSession = mock(MockMode.autofill)
        mockSdkContext = mock(MockMode.autofill)
        mockSdkLogger = mock(MockMode.autofill)

        disableOrganizer = DisableOrganizer(
            mobileEngageDisableStateMachine = mockMEDisableStateMachine,
            sdkContext = mockSdkContext,
            mockSession,
            sdkLogger = mockSdkLogger
        )
    }

    @Test
    fun testDisable_shouldActivate_MEDisableStateMachine_andEndSession() = runTest {
        disableOrganizer.disable()

        verifySuspend { mockSdkContext.setSdkState(SdkState.Initialized) }
        verifySuspend { mockMEDisableStateMachine.activate() }
        verifySuspend { mockSession.endSession() }
    }

    @Test
    fun testDisable_shouldThrowException_whenStateMachineActivation_fails() = runTest {
        everySuspend { mockMEDisableStateMachine.activate() } returns Result.failure(RuntimeException("test exception"))

        shouldThrow<RuntimeException> { disableOrganizer.disable() }

        verifySuspend { mockSdkContext.setSdkState(SdkState.Initialized) }
        verifySuspend { mockMEDisableStateMachine.activate() }
        verifySuspend(VerifyMode.exactly(0)) { mockSession.endSession() }
    }

    @Test
    fun testDisableWithValidation_shouldActivateMEDisableStateMachine() = runTest {
        everySuspend { mockSdkContext.isEnabledState() } returns true

        disableOrganizer.disableWithValidation()

        verifySuspend { mockSdkContext.setSdkState(SdkState.Initialized) }
        verifySuspend { mockMEDisableStateMachine.activate() }
        verifySuspend { mockSession.endSession() }
    }

    @Test
    fun testDisableWithValidation_shouldThrowException_whenSdkIsNotEnabled() = runTest {
        everySuspend { mockSdkContext.isEnabledState() } returns false

        val exception = shouldThrow<SdkAlreadyDisabledException> {
            disableOrganizer.disableWithValidation()
        }
        exception.message shouldBe "SAP Engagement Cloud SDK was already disabled!"

        verifySuspend(VerifyMode.exactly(0)) {
            mockMEDisableStateMachine.activate()
        }
    }

    @Test
    fun testDisableWithValidation_shouldThrowException_whenStateMachineActivation_fails() = runTest {
        everySuspend { mockSdkContext.isEnabledState() } returns true
        everySuspend { mockMEDisableStateMachine.activate() } returns Result.failure(RuntimeException("test exception"))

        shouldThrow<RuntimeException> { disableOrganizer.disableWithValidation() }

        verifySuspend { mockSdkContext.setSdkState(SdkState.Initialized) }
        verifySuspend { mockMEDisableStateMachine.activate() }
        verifySuspend(VerifyMode.exactly(0)) { mockSession.endSession() }
    }
}