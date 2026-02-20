package com.sap.ec.mobileengage.inapp

import com.sap.ec.core.log.Logger
import com.sap.ec.mobileengage.action.EventActionFactoryApi
import com.sap.ec.mobileengage.action.models.BasicAppEventActionModel
import com.sap.ec.mobileengage.action.models.BasicCopyToClipboardActionModel
import com.sap.ec.mobileengage.action.models.BasicCustomEventActionModel
import com.sap.ec.mobileengage.action.models.BasicDismissActionModel
import com.sap.ec.mobileengage.action.models.BasicInAppButtonClickedActionModel
import com.sap.ec.mobileengage.action.models.BasicOpenExternalUrlActionModel
import com.sap.ec.mobileengage.action.models.RequestPushPermissionActionModel
import com.sap.ec.mobileengage.inapp.jsbridge.InAppJsBridgeData
import com.sap.ec.util.JsonUtil
import io.mockk.coVerify
import io.mockk.mockk
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
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put
import org.junit.Before
import org.junit.Test
import kotlin.test.AfterTest

@OptIn(ExperimentalCoroutinesApi::class)
class InAppJsBridgeTests {
    private companion object {
        const val DISMISS_ID = "dismissId"
        const val BUTTON_ID = "buttonId"
        const val TRACKING_INFO = """{"key":"value"}"""
        val reporting = buildJsonObject {
            put("reportingKey", "reportingValue")
        }.toString()
        val payloadMap = mapOf("pay" to "load")
    }

    private lateinit var inAppJsBridgeData: InAppJsBridgeData
    private lateinit var mockActionFactory: EventActionFactoryApi
    private lateinit var applicationScope: CoroutineScope
    private lateinit var json: Json
    private lateinit var mockLogger: Logger
    private lateinit var inAppJsBridge: InAppJsBridge

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        inAppJsBridgeData = InAppJsBridgeData(DISMISS_ID, TRACKING_INFO)
        mockActionFactory = mockk(relaxed = true)
        mockLogger = mockk(relaxed = true)
        applicationScope = TestScope(StandardTestDispatcher())
        json = JsonUtil.json
        inAppJsBridge =
            InAppJsBridge(inAppJsBridgeData, mockActionFactory, applicationScope, json, mockLogger)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun handleInAppAction_shouldCall_actionFactory_withBasicCustomEventActionModel() = runTest {
        val eventName = "testCustomEvent"
        val testEventString = buildJsonObject {
            put("type", "MECustomEvent")
            put("reporting", reporting)
            put("name", eventName)
            put("payload", json.encodeToJsonElement(payloadMap))
        }.toString()

        val expectedActionModel = BasicCustomEventActionModel(reporting, eventName, payloadMap)

        inAppJsBridge.handleInAppAction(testEventString)

        advanceUntilIdle()

        coVerify {
            mockActionFactory.create(expectedActionModel)
        }
    }

    @Test
    fun handleInAppAction_shouldCall_actionFactory_withBasicButtonClickedActionModel() = runTest {
        val testEventString = buildJsonObject {
            put("type", "inAppButtonClicked")
            put("buttonId", BUTTON_ID)
            put("reporting", reporting)
        }.toString()

        val expectedActionModel =
            BasicInAppButtonClickedActionModel(reporting, TRACKING_INFO)

        inAppJsBridge.handleInAppAction(testEventString)

        advanceUntilIdle()

        coVerify {
            mockActionFactory.create(expectedActionModel)
        }
    }

    @Test
    fun handleInAppAction_shouldCall_actionFactory_withBasicAppEventActionModel() = runTest {
        val eventName = "testAppEvent"
        val testEventString = buildJsonObject {
            put("type", "MEAppEvent")
            put("reporting", reporting)
            put("name", eventName)
            put("payload", json.encodeToJsonElement(payloadMap))
        }.toString()

        val expectedActionModel = BasicAppEventActionModel(reporting, eventName, payloadMap)

        inAppJsBridge.handleInAppAction(testEventString)

        advanceUntilIdle()

        coVerify {
            mockActionFactory.create(expectedActionModel)
        }
    }

    @Test
    fun handleInAppAction_shouldCall_actionFactory_withRequestPushPermissionActionModel() =
        runTest {
            val testEventString = buildJsonObject {
                put("type", "RequestPushPermission")
            }.toString()

            val expectedActionModel = RequestPushPermissionActionModel()

            inAppJsBridge.handleInAppAction(testEventString)

            advanceUntilIdle()

            coVerify {
                mockActionFactory.create(expectedActionModel)
            }
        }

    @Test
    fun handleInAppAction_shouldCall_actionFactory_withBasicOpenExternalUrlActionModel() = runTest {
        val url = "https://test.com"
        val testEventString = buildJsonObject {
            put("type", "OpenExternalUrl")
            put("reporting", reporting)
            put("url", url)
        }.toString()

        val expectedActionModel = BasicOpenExternalUrlActionModel(reporting, url)

        inAppJsBridge.handleInAppAction(testEventString)

        advanceUntilIdle()

        coVerify {
            mockActionFactory.create(expectedActionModel)
        }
    }

    @Test
    fun handleInAppAction_shouldCall_actionFactory_withBasicDismissActionModel() = runTest {
        val testEventString = buildJsonObject {
            put("type", "Dismiss")
            put("reporting", reporting)
        }.toString()

        val expectedActionModel = BasicDismissActionModel(reporting, DISMISS_ID)

        inAppJsBridge.handleInAppAction(testEventString)

        advanceUntilIdle()

        coVerify {
            mockActionFactory.create(expectedActionModel)
        }
    }

    @Test
    fun handleInAppAction_shouldCall_actionFactory_withBasicCopyToClipboardActionModel() = runTest {
        val text = "copy me to clipboard"
        val testEventString = buildJsonObject {
            put("type", "copyToClipboard")
            put("reporting", reporting)
            put("text", text)
        }.toString()

        val expectedActionModel = BasicCopyToClipboardActionModel(reporting, text)

        inAppJsBridge.handleInAppAction(testEventString)

        advanceUntilIdle()

        coVerify {
            mockActionFactory.create(expectedActionModel)
        }
    }

    @Test
    fun handleInAppAction_shouldCreateErrorLog_ifActionModelCannotBeParsed() = runTest {
        val text = "copy me to clipboard"
        val testEventString = buildJsonObject {
            put("reporting", reporting)
            put("text", text)
        }.toString()

        inAppJsBridge.handleInAppAction(testEventString)

        advanceUntilIdle()

        coVerify(exactly = 0) { mockActionFactory.create(any()) }
        coVerify {
            mockLogger.error(
                "Failed to parse actionModel from inapp action data.",
                any<Throwable>()
            )
        }
    }
}