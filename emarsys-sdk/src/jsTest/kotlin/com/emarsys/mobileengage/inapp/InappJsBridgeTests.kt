package com.emarsys.mobileengage.inapp

import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.actions.ButtonClickedAction
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.action.models.BasicAppEventActionModel
import com.emarsys.mobileengage.action.models.BasicButtonClickedActionModel
import com.emarsys.mobileengage.action.models.BasicCopyToClipboardActionModel
import com.emarsys.mobileengage.action.models.BasicCustomEventActionModel
import com.emarsys.mobileengage.action.models.BasicDismissActionModel
import com.emarsys.mobileengage.action.models.BasicOpenExternalUrlActionModel
import com.emarsys.mobileengage.action.models.RequestPushPermissionActionModel
import com.emarsys.util.JsonUtil
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import web.window.window
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InappJsBridgeTests {
    private companion object {
        val testAction = ButtonClickedAction(BasicButtonClickedActionModel("1", "testId"))
    }

    private lateinit var inappJsBridge: InAppJsBridgeApi
    private lateinit var mockActionFactory: ActionFactoryApi<ActionModel>
    private lateinit var json: Json
    private lateinit var sdkScope: CoroutineScope

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        sdkScope = TestScope(StandardTestDispatcher())
        json = JsonUtil.json
        mockActionFactory = mock()

        inappJsBridge = InAppJsBridge(mockActionFactory, json, sdkScope)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun buttonClicked_shouldTrigger_actionFactory() = runTest {
        val testActionModel = BasicButtonClickedActionModel("1", "inputButtonId")
        everySuspend {
            mockActionFactory.create(action = testActionModel)
        } returns testAction

        inappJsBridge.register()

        window.asDynamic()["EMSInappWebBridge"].buttonClicked("""{"id":"1","buttonId":"inputButtonId"}""")

        advanceUntilIdle()

        verifySuspend {
            mockActionFactory.create(testActionModel)
        }
    }

    @Test
    fun triggerMEEvent_shouldTrigger_actionFactory() = runTest {
        val testActionModel = BasicCustomEventActionModel("customEventName", mapOf("key" to "value"))
        everySuspend {
            mockActionFactory.create(action = testActionModel)
        } returns testAction

        inappJsBridge.register()

        window.asDynamic()["EMSInappWebBridge"].triggerMEEvent("""{"name":"customEventName","payload":{"key":"value"},"id":"1"}""")

        advanceUntilIdle()

        verifySuspend {
            mockActionFactory.create(testActionModel)
        }
    }

    @Test
    fun triggerAppEvent_shouldTrigger_actionFactory() = runTest {
        val testActionModel = BasicAppEventActionModel("appEventName", mapOf("key" to "value"))
        everySuspend {
            mockActionFactory.create(action = testActionModel)
        } returns testAction

        inappJsBridge.register()

        window.asDynamic()["EMSInappWebBridge"].triggerAppEvent("""{"name":"appEventName","payload":{"key":"value"},"id":"1"}""")

        advanceUntilIdle()

        verifySuspend {
            mockActionFactory.create(testActionModel)
        }
    }

    @Test
    fun requestPushPermission_shouldTrigger_actionFactory() = runTest {
        val testActionModel = RequestPushPermissionActionModel
        everySuspend {
            mockActionFactory.create(action = testActionModel)
        } returns testAction

        inappJsBridge.register()

        window.asDynamic()["EMSInappWebBridge"].requestPushPermission("""{"id":"1"}""")

        advanceUntilIdle()

        verifySuspend {
            mockActionFactory.create(testActionModel)
        }
    }

    @Test
    fun openExternalLink_shouldTrigger_actionFactory() = runTest {
        val testActionModel = BasicOpenExternalUrlActionModel("https://sap.com")
        everySuspend {
            mockActionFactory.create(action = testActionModel)
        } returns testAction

        inappJsBridge.register()

        window.asDynamic()["EMSInappWebBridge"].openExternalLink("""{"url":"https://sap.com","id":"1"}""")

        advanceUntilIdle()

        verifySuspend {
            mockActionFactory.create(testActionModel)
        }
    }

    @Test
    fun dismiss_shouldTrigger_actionFactory() = runTest {
        val testActionModel = BasicDismissActionModel("dismiss")
        everySuspend {
            mockActionFactory.create(action = testActionModel)
        } returns testAction

        inappJsBridge.register()

        window.asDynamic()["EMSInappWebBridge"].close("""{"id":"1"}""")

        advanceUntilIdle()

        verifySuspend {
            mockActionFactory.create(testActionModel)
        }
    }

    @Test
    fun copyToClipboard_shouldTrigger_actionFactory() = runTest {
        val testActionModel = BasicCopyToClipboardActionModel("testValue")
        everySuspend {
            mockActionFactory.create(action = testActionModel)
        } returns testAction

        inappJsBridge.register()

        window.asDynamic()["EMSInappWebBridge"].copyToClipboard("""{"text":"testValue","id":"1"}""")

        advanceUntilIdle()

        verifySuspend {
            mockActionFactory.create(testActionModel)
        }
    }
}