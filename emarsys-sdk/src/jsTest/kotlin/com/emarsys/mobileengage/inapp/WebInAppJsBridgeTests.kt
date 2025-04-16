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
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import web.window.window
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WebInAppJsBridgeTests {
    private companion object {
        const val ID = "1"
        const val CAMPAIGN_ID = "testCampaignId"
        const val REPORTING = """{"reportingKey":"reportingValue"}"""
        const val TRACKING_INFO = """{"trackingInfoKey":"trackingInfoValue"}"""
        val TEST_ACTION =
            ReportingAction(
                BasicInAppButtonClickedActionModel(REPORTING, TRACKING_INFO),
                mock(MockMode.autofill)
            )
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
        val testActionModel = BasicInAppButtonClickedActionModel(REPORTING)
        everySuspend {
            mockActionFactory.create(action = testActionModel)
        } returns TEST_ACTION

        inappJsBridge.register()

        window.asDynamic()["EMSInappWebBridge"].buttonClicked(createTestJson())

        advanceUntilIdle()

        verifySuspend {
            mockActionFactory.create(testActionModel)
        }
    }

    @Test
    fun triggerMECustomEvent_shouldTrigger_actionFactory() = runTest {
        val testActionModel =
            BasicCustomEventActionModel(REPORTING, "customEventName", mapOf("key" to "value"))
        val testJsonString =
            createTestJson(name = "customEventName", payload = buildJsonObject { put("key", "value") })
        everySuspend {
            mockActionFactory.create(action = testActionModel)
        } returns TEST_ACTION

        inappJsBridge.register()

        window.asDynamic()["EMSInappWebBridge"].triggerMEEvent(testJsonString)

        advanceUntilIdle()

        verifySuspend {
            mockActionFactory.create(testActionModel)
        }
    }

    @Test
    fun triggerAppEvent_shouldTrigger_actionFactory() = runTest {
        val testActionModel =
            BasicAppEventActionModel(REPORTING, "appEventName", mapOf("key" to "value"))
        val testJsonString = createTestJson(name = "appEventName", payload = buildJsonObject { put("key", "value") })
        everySuspend {
            mockActionFactory.create(action = testActionModel)
        } returns TEST_ACTION

        inappJsBridge.register()

        window.asDynamic()["EMSInappWebBridge"].triggerAppEvent(testJsonString)

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

        window.asDynamic()["EMSInappWebBridge"].requestPushPermission(createTestJson())

        advanceUntilIdle()

        verifySuspend {
            mockActionFactory.create(testActionModel)
        }
    }

    @Test
    fun openExternalLink_shouldTrigger_actionFactory() = runTest {
        val testActionModel = BasicOpenExternalUrlActionModel(REPORTING, "https://sap.com")
        val testJsonString = createTestJson(url = "https://sap.com")
        everySuspend {
            mockActionFactory.create(action = testActionModel)
        } returns TEST_ACTION

        inappJsBridge.register()

        window.asDynamic()["EMSInappWebBridge"].openExternalLink(testJsonString)

        advanceUntilIdle()

        verifySuspend {
            mockActionFactory.create(testActionModel)
        }
    }

    @Test
    fun dismiss_shouldTrigger_actionFactory() = runTest {
        val testActionModel = BasicDismissActionModel(REPORTING, CAMPAIGN_ID)
        everySuspend {
            mockActionFactory.create(action = testActionModel)
        } returns TEST_ACTION

        inappJsBridge.register()

        window.asDynamic()["EMSInappWebBridge"].close(createTestJson())

        advanceUntilIdle()

        verifySuspend {
            mockActionFactory.create(testActionModel)
        }
    }

    @Test
    fun copyToClipboard_shouldTrigger_actionFactory() = runTest {
        val testActionModel = BasicCopyToClipboardActionModel(REPORTING, "testValue")
        val testJsonString = createTestJson(text = "testValue")
        everySuspend {
            mockActionFactory.create(action = testActionModel)
        } returns TEST_ACTION

        inappJsBridge.register()

        window.asDynamic()["EMSInappWebBridge"].copyToClipboard(testJsonString)

        advanceUntilIdle()

        verifySuspend {
            mockActionFactory.create(testActionModel)
        }
    }

    private fun createTestJson(
        name: String? = null,
        payload: JsonObject? = null,
        url: String? = null,
        text: String? = null
    ): String {
        return buildJsonObject {
            put("id", ID)
            put("reporting", buildJsonObject { put("reportingKey", "reportingValue") }.toString())
            name?.let { put("name", name) }
            payload?.let { put("payload", payload) }
            url?.let { put("url", url) }
            text?.let { put("text", text) }
        }.toString()
    }
}