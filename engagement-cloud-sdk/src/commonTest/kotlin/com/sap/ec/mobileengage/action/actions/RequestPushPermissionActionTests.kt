package com.sap.ec.mobileengage.action.actions

import com.sap.ec.core.permission.PermissionHandlerApi
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class RequestPushPermissionActionTests {

    @Test
    fun invoke_shouldCallPermissionHandler() = runTest {
        val mockPermissionHandler = mock<PermissionHandlerApi> {
            everySuspend { requestPushPermission() } returns Unit
        }

        val requestPushPermissionAction = RequestPushPermissionAction(mockPermissionHandler)
        requestPushPermissionAction()

        verifySuspend { mockPermissionHandler.requestPushPermission() }
    }
}