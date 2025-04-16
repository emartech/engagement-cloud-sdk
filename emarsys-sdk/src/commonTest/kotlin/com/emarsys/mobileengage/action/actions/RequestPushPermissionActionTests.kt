package com.emarsys.mobileengage.action.actions

import com.emarsys.core.permission.PermissionHandlerApi
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