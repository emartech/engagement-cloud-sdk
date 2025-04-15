package com.emarsys.mobileengage.inapp

import com.emarsys.mobileengage.action.EventActionFactoryApi
import com.emarsys.mobileengage.action.actions.ReportingAction
import com.emarsys.mobileengage.action.models.BasicAppEventActionModel
import com.emarsys.mobileengage.action.models.BasicCopyToClipboardActionModel
import com.emarsys.mobileengage.action.models.BasicCustomEventActionModel
import com.emarsys.mobileengage.action.models.BasicDismissActionModel
import com.emarsys.mobileengage.action.models.BasicInAppButtonClickedActionModel
import com.emarsys.mobileengage.action.models.BasicOpenExternalUrlActionModel
import com.emarsys.mobileengage.action.models.RequestPushPermissionActionModel
import com.emarsys.util.JsonUtil
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
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
class WebInAppJsBridgeTests {
    private companion object {
        const val ID = "1"
        const val CAMPAIGN_ID = "testCampaignId"
        val TEST_ACTION =
            ReportingAction(BasicInAppButtonClickedActionModel(ID, CAMPAIGN_ID), mock(MockMode.autofill))
    }

    private lateinit var inappJsBridge: InAppJsBridgeApi
    private lateinit var mockActionFactory: EventActionFactoryApi
    private lateinit var json: Json
    private lateinit var sdkDispatcher: CoroutineDispatcher

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        sdkDispatcher = StandardTestDispatcher()
        json = JsonUtil.json
        mockActionFactory = mock(MockMode.autoUnit)

        inappJsBridge = WebInAppJsBridge(mockActionFactory, json, sdkDispatcher, CAMPAIGN_ID)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun buttonClicked_shouldTrigger_actionFactory() = runTest {
        val testActionModel = BasicInAppButtonClickedActionModel(ID)
        everySuspend {
            mockActionFactory.create(action = testActionModel)
        } returns TEST_ACTION

        inappJsBridge.register()

        window.asDynamic()["EMSInappWebBridge"].buttonClicked("""{"id":"$ID"}""")

        advanceUntilIdle()

        verifySuspend {
            mockActionFactory.create(testActionModel)
        }
    }

    @Test
    fun triggerMEEvent_shouldTrigger_actionFactory() = runTest {
        val testActionModel =
            BasicCustomEventActionModel("customEventName", mapOf("key" to "value"))
        everySuspend {
            mockActionFactory.create(action = testActionModel)
        } returns TEST_ACTION

        inappJsBridge.register()

        window.asDynamic()["EMSInappWebBridge"].triggerMEEvent("""{"name":"customEventName","payload":{"key":"value"},"id":"$ID"}""")

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
        } returns TEST_ACTION

        inappJsBridge.register()

        window.asDynamic()["EMSInappWebBridge"].triggerAppEvent("""{"name":"appEventName","payload":{"key":"value"},"id":"$ID"}""")

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
        } returns TEST_ACTION

        inappJsBridge.register()

        window.asDynamic()["EMSInappWebBridge"].requestPushPermission("""{"id":"$ID"}""")

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
        } returns TEST_ACTION

        inappJsBridge.register()

        window.asDynamic()["EMSInappWebBridge"].openExternalLink("""{"url":"https://sap.com","id":"$ID"}""")

        advanceUntilIdle()

        verifySuspend {
            mockActionFactory.create(testActionModel)
        }
    }

    @Test
    fun dismiss_shouldTrigger_actionFactory() = runTest {
        val testActionModel = BasicDismissActionModel(CAMPAIGN_ID)
        everySuspend {
            mockActionFactory.create(action = testActionModel)
        } returns TEST_ACTION

        inappJsBridge.register()

        window.asDynamic()["EMSInappWebBridge"].close("""{"id":"$ID"}""")

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
        } returns TEST_ACTION

        inappJsBridge.register()

        window.asDynamic()["EMSInappWebBridge"].copyToClipboard("""{"text":"testValue","id":"$ID"}""")

        advanceUntilIdle()

        verifySuspend {
            mockActionFactory.create(testActionModel)
        }
    }
}