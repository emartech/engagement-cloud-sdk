package com.sap.ec.mobileengage.inapp

import com.sap.ec.core.log.Logger
import com.sap.ec.mobileengage.action.ActionFactoryApi
import com.sap.ec.mobileengage.action.actions.Action
import com.sap.ec.mobileengage.action.models.ActionModel
import com.sap.ec.mobileengage.action.models.BasicCustomEventActionModel
import com.sap.ec.mobileengage.action.models.BasicDismissActionModel
import com.sap.ec.mobileengage.action.models.BasicInAppButtonClickedActionModel
import com.sap.ec.mobileengage.inapp.jsbridge.InAppJsBridgeData
import com.sap.ec.mobileengage.inapp.presentation.InAppType
import com.sap.ec.util.JsonUtil
import dev.mokkery.MockMode
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import platform.WebKit.WKScriptMessage
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

private class FakeWKScriptMessage(
    private val fakeName: String,
    private val fakeBody: Any
) : WKScriptMessage() {
    override fun name(): String = fakeName
    override fun body(): Any = fakeBody
}

@OptIn(ExperimentalCoroutinesApi::class)
class InAppJsBridgeTests {
    private companion object {
        const val DISMISS_ID = "dismissId"
        const val TRACKING_INFO = """{"key":"value"}"""
    }

    private lateinit var mockActionFactory: ActionFactoryApi<ActionModel>
    private lateinit var mockLogger: Logger
    private lateinit var json: Json
    private lateinit var inAppJsBridge: InAppJsBridge

    @BeforeTest
    fun setup() {
        val dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)
        mockActionFactory = mock(MockMode.autofill)
        mockLogger = mock(MockMode.autofill)
        json = JsonUtil.json

        inAppJsBridge = InAppJsBridge(
            actionFactory = mockActionFactory,
            inAppJsBridgeData = InAppJsBridgeData(DISMISS_ID, TRACKING_INFO, InAppType.OVERLAY),
            mainDispatcher = dispatcher,
            sdkDispatcher = dispatcher,
            logger = mockLogger,
            json = json
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun actions_shouldExecuteSequentially_dismissWaitsForPrecedingAction() = runTest {
        val executionOrder = mutableListOf<String>()
        val customEventGate = CompletableDeferred<Unit>()

        val mockCustomEventAction: Action<*> = mock(MockMode.autofill) {
            everySuspend { invoke(any()) } calls {
                customEventGate.await()
                executionOrder.add("customEvent")
            }
        }
        val mockDismissAction: Action<*> = mock(MockMode.autofill) {
            everySuspend { invoke(any()) } calls {
                executionOrder.add("dismiss")
            }
        }

        everySuspend { mockActionFactory.create(any<BasicCustomEventActionModel>()) } returns mockCustomEventAction
        everySuspend { mockActionFactory.create(any<BasicDismissActionModel>()) } returns mockDismissAction

        val contentController = inAppJsBridge.registerContentController()

        val customEventBody = mapOf("name" to "testEvent", "reporting" to "rep")
        val dismissBody = mapOf("reporting" to "rep")

        inAppJsBridge.userContentController(contentController, FakeWKScriptMessage("triggerMEEvent", customEventBody))
        inAppJsBridge.userContentController(contentController, FakeWKScriptMessage("close", dismissBody))

        advanceUntilIdle()

        assertTrue(executionOrder.isEmpty(), "Dismiss should not have executed while customEvent is suspended")

        customEventGate.complete(Unit)
        advanceUntilIdle()

        assertEquals(listOf("customEvent", "dismiss"), executionOrder, "Actions should execute sequentially in order")
    }

    @Test
    fun buttonClicked_shouldPassEmbeddedMessagingInAppType_whenBridgeDataHasEmbeddedMessaging() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val embeddedMockActionFactory: ActionFactoryApi<ActionModel> = mock(MockMode.autofill)
        val embeddedMockLogger: Logger = mock(MockMode.autofill)

        val embeddedBridge = InAppJsBridge(
            actionFactory = embeddedMockActionFactory,
            inAppJsBridgeData = InAppJsBridgeData(DISMISS_ID, TRACKING_INFO, InAppType.EMBEDDED_MESSAGING),
            mainDispatcher = dispatcher,
            sdkDispatcher = dispatcher,
            logger = embeddedMockLogger,
            json = json
        )

        var capturedModel: ActionModel? = null
        val mockAction: Action<*> = mock(MockMode.autofill) {
            everySuspend { invoke(any()) } returns Unit
        }
        everySuspend { embeddedMockActionFactory.create(any()) } calls { args ->
            capturedModel = args.arg(0)
            mockAction
        }

        val contentController = embeddedBridge.registerContentController()
        val buttonClickedBody = mapOf("reporting" to "rep")

        embeddedBridge.userContentController(contentController, FakeWKScriptMessage("buttonClicked", buttonClickedBody))

        advanceUntilIdle()

        assertIs<BasicInAppButtonClickedActionModel>(capturedModel)
        assertEquals(InAppType.EMBEDDED_MESSAGING, (capturedModel as BasicInAppButtonClickedActionModel).inAppType)
        assertEquals(TRACKING_INFO, (capturedModel as BasicInAppButtonClickedActionModel).trackingInfo)
    }
}
