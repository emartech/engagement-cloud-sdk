package com.emarsys.core.actions

import com.emarsys.core.actions.launchapplication.LaunchApplicationHandlerApi
import com.emarsys.mobileengage.action.actions.LaunchApplicationAction
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class LaunchApplicationActionTests {
    private lateinit var mockLaunchApplicationHandler: LaunchApplicationHandlerApi
    private lateinit var launchApplicationAction: LaunchApplicationAction

    @BeforeTest
    fun setup() {
        mockLaunchApplicationHandler = mock()
        everySuspend { mockLaunchApplicationHandler.launchApplication() } returns Unit

        launchApplicationAction = LaunchApplicationAction(mockLaunchApplicationHandler)
    }

    @Test
    fun testInvoke_shouldCall_launchApplicationHandler() = runTest {
        launchApplicationAction.invoke(null)

        verifySuspend { mockLaunchApplicationHandler.launchApplication() }
    }
}