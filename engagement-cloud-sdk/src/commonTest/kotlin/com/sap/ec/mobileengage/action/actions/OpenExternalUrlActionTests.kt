package com.sap.ec.mobileengage.action.actions

import com.sap.ec.core.url.ExternalUrlOpenerApi
import com.sap.ec.mobileengage.action.models.PresentableOpenExternalUrlActionModel
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class OpenExternalUrlActionTests {

    @Test
    fun invoke_shouldCallExternalUrlOpener() = runTest {
        val actionModel = PresentableOpenExternalUrlActionModel(
            id = "123",
            reporting = """{"reportingKey":"reportingValue"}""",
            title = "Engagement Cloud",
            url = "https://www.sap.com"
        )
        val mockExternalUrlOpener = mock<ExternalUrlOpenerApi> {
            everySuspend { open(actionModel) } returns Unit
        }

        val openExternalUrlAction = OpenExternalUrlAction(
            actionModel,
            mockExternalUrlOpener
        )
        openExternalUrlAction()

        verifySuspend { mockExternalUrlOpener.open(actionModel) }
    }
}