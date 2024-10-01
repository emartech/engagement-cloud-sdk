package com.emarsys.core.actions

import com.emarsys.mobileengage.action.actions.Action
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class ActionHandlerTests {
    private lateinit var actionHandler: ActionHandler

    @BeforeTest
    fun setup() {
        actionHandler = ActionHandler()
    }

    @Test
    fun testHandleActions_should_handle_actionsInOrder() = runTest {
        val mockDismissAction: Action<Unit> = mock()
        everySuspend { mockDismissAction.invoke() } returns Unit
        val mockAppEventAction: Action<Unit> = mock()
        everySuspend { mockAppEventAction.invoke() } returns Unit
        val mockCustomEventAction: Action<Unit> = mock()
        everySuspend { mockCustomEventAction.invoke() } returns Unit
        val mandatoryActions = listOf(mockDismissAction, mockAppEventAction)

        actionHandler.handleActions(mandatoryActions, mockCustomEventAction)

        verifySuspend(VerifyMode.order) {
            mockDismissAction.invoke()
            mockAppEventAction.invoke()
            mockCustomEventAction.invoke()
        }
    }
}