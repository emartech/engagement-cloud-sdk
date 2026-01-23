package com.emarsys.mobileengage.config

import com.emarsys.api.SdkState
import com.emarsys.context.SdkContextApi
import com.emarsys.core.log.Logger
import com.emarsys.core.state.StateMachineApi
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class FollowUpChangeAppCodeOrganizerTests {
    private lateinit var changeAppCodeStateMachine: StateMachineApi
    private lateinit var sdkContext: SdkContextApi
    private lateinit var logger: Logger
    private lateinit var followUpChangeAppCodeOrganizer: FollowUpChangeAppCodeOrganizer

    @BeforeTest
    fun setUp() {
        changeAppCodeStateMachine = mock(MockMode.autofill)
        sdkContext = mock(MockMode.autofill)
        logger = mock(MockMode.autofill)
        followUpChangeAppCodeOrganizer = FollowUpChangeAppCodeOrganizer(
            changeAppCodeStateMachine,
            sdkContext,
            logger
        )
    }

    @Test
    fun testOrganize_should_setSdkToOnHold_active_ChangeAppCodeStateMachine_thenReactivateSdk() = runTest {
        everySuspend {
            changeAppCodeStateMachine.activate()
        } returns Result.success(Unit)

        followUpChangeAppCodeOrganizer.organize()

        verifySuspend(VerifyMode.order) {
            sdkContext.setSdkState(SdkState.OnHold)
            changeAppCodeStateMachine.activate()
            sdkContext.setSdkState(SdkState.Active)
        }
    }

    @Test
    fun testOrganize_should_setSdkToOnHold_active_ChangeAppCodeStateMachine_thenReactivateSdkAndLogFailure() = runTest {
        val exception = Exception("Activation failed")
        everySuspend {
            changeAppCodeStateMachine.activate()
        } returns Result.failure(exception)

        followUpChangeAppCodeOrganizer.organize()

        verifySuspend(VerifyMode.order) {
            sdkContext.setSdkState(SdkState.OnHold)
            changeAppCodeStateMachine.activate()
            logger.error("Failed to activate ChangeAppCodeStateMachine during app code change", exception)
            sdkContext.setSdkState(SdkState.Active)
        }
    }


}