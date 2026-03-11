package com.sap.ec.mobileengage.inapp.iframe

import com.sap.ec.core.log.Logger
import com.sap.ec.mobileengage.action.EventActionFactoryApi
import com.sap.ec.mobileengage.action.actions.Action
import com.sap.ec.mobileengage.action.models.BasicCustomEventActionModel
import com.sap.ec.mobileengage.action.models.BasicDismissActionModel
import com.sap.ec.mobileengage.action.models.BasicInAppButtonClickedActionModel
import com.sap.ec.mobileengage.action.models.BasicOpenExternalUrlActionModel
import com.sap.ec.mobileengage.action.models.HtmlTarget
import com.sap.ec.mobileengage.inapp.InAppMessage
import com.sap.ec.mobileengage.inapp.presentation.InAppType
import com.sap.ec.mobileengage.inapp.toIframeId
import com.sap.ec.util.JsonUtil
import dev.mokkery.MockMode
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import kotlinx.coroutines.CompletableDeferred
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
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class MessageChannelProviderTests {
    private companion object {
        const val DISMISS_ID = "dismissId"
        const val REPORTING = "reporting"
        const val TRACKING_INFO = """{"key":"value"}"""
        val testInAppMessage = InAppMessage(
            dismissId = DISMISS_ID,
            trackingInfo = TRACKING_INFO,
            content = "testContent"
        )
    }

    private lateinit var mockEventActionFactory: EventActionFactoryApi
    private lateinit var mockIframeContainerResizer: IframeContainerResizerApi
    private lateinit var mockLogger: Logger
    private lateinit var json: Json
    private lateinit var messageChannelProvider: MessageChannelProviderApi

    @BeforeTest
    fun setup() {
        val dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)
        mockEventActionFactory = mock(MockMode.autofill)
        mockIframeContainerResizer = mock(MockMode.autofill)
        mockLogger = mock(MockMode.autofill)
        json = JsonUtil.json

        messageChannelProvider =
            MessageChannelProvider(
                mockEventActionFactory,
                TestScope(dispatcher),
                mockIframeContainerResizer,
                mockLogger,
                json
            )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun provide_shouldReturnMessageChannel_withRegisteredMessageHandler() = runTest {
        val testActionModel = BasicOpenExternalUrlActionModel(
            REPORTING,
            "https://sap.com",
            HtmlTarget.TOP
        )
        val actionInvoked = CompletableDeferred<Unit>()
        val mockAction: Action<*> = mock(MockMode.autofill) {
            everySuspend { invoke() } calls {
                actionInvoked.complete(Unit)
            }
        }
        everySuspend { mockEventActionFactory.create(testActionModel) } returns mockAction

        val channel = messageChannelProvider.provide(testInAppMessage)

        val messageWithType = buildJsonObject {
            put("reporting", REPORTING)
            put("url", "https://sap.com")
            put("target", "_top")
            put("type", "OpenExternalUrl")
        }

        channel.port2.postMessage(JSON.parse(json.encodeToString(messageWithType)))

        advanceUntilIdle()

        actionInvoked.await()
        verifySuspend {
            mockEventActionFactory.create(testActionModel)
            mockAction.invoke()
        }
    }

    @Test
    fun provide_shouldReturnMessageChannel_andShouldNotCrash_whenMessageDataCantBeParsed() =
        runTest {
            val loggerInvoked = CompletableDeferred<Unit>()

            everySuspend { mockLogger.error(any<String>(), any<Throwable>()) } calls {
                loggerInvoked.complete(Unit)
            }

            val channel = messageChannelProvider.provide(testInAppMessage)

            val messageWithType = buildJsonObject {
                put("unknownKey", "unknownValue")
            }

            channel.port2.postMessage(JSON.parse(json.encodeToString(messageWithType)))

            advanceUntilIdle()

            loggerInvoked.await()
            verifySuspend(VerifyMode.exactly(0)) {
                mockEventActionFactory.create(any())
            }
        }

    @Test
    fun provide_shouldAmend_theActionModel_inCaseOfDismiss() = runTest {
        val testActionModel = BasicDismissActionModel()
        val amendedActionModel = testActionModel.copy(dismissId = DISMISS_ID, inAppType = InAppType.OVERLAY)
        val actionInvoked = CompletableDeferred<Unit>()
        val mockAction: Action<*> = mock(MockMode.autofill) {
            everySuspend { invoke() } calls {
                actionInvoked.complete(Unit)
            }
        }
        everySuspend { mockEventActionFactory.create(amendedActionModel) } returns mockAction

        val channel = messageChannelProvider.provide(testInAppMessage)

        val messageWithType = buildJsonObject {
            put("type", "Dismiss")
        }

        channel.port2.postMessage(JSON.parse(json.encodeToString(messageWithType)))

        advanceUntilIdle()

        actionInvoked.await()
        verifySuspend {
            mockEventActionFactory.create(amendedActionModel)
            mockAction.invoke()
        }
    }

    @Test
    fun provide_shouldAmend_theActionModel_inCaseOfButtonClick() = runTest {
        val testActionModel = BasicInAppButtonClickedActionModel(
            REPORTING,
            "",
            InAppType.OVERLAY
        )
        val amendedActionModel = testActionModel.copy(trackingInfo = TRACKING_INFO, inAppType = InAppType.OVERLAY)
        val actionInvoked = CompletableDeferred<Unit>()
        val mockAction: Action<*> = mock(MockMode.autofill) {
            everySuspend { invoke() } calls {
                actionInvoked.complete(Unit)
            }
        }
        everySuspend { mockEventActionFactory.create(amendedActionModel) } returns mockAction

        val channel = messageChannelProvider.provide(testInAppMessage)

        val messageWithType = buildJsonObject {
            put("reporting", REPORTING)
            put("type", "inAppButtonClicked")
        }

        channel.port2.postMessage(JSON.parse(json.encodeToString(messageWithType)))

        advanceUntilIdle()

        actionInvoked.await()
        verifySuspend {
            mockEventActionFactory.create(amendedActionModel)
            mockAction.invoke()
        }
    }

    @Test
    fun provide_shouldRegisterMessageHandler_thatHandles_resizeMessagesProperly() = runTest {
        val testHeight = 321

        val resizeCompleted = CompletableDeferred<Unit>()
        every { mockIframeContainerResizer.resize(DISMISS_ID.toIframeId(), testHeight) } calls {
            resizeCompleted.complete(Unit)
        }

        val channel = messageChannelProvider.provide(testInAppMessage)

        val messageWithType = buildJsonObject {
            put("height", testHeight)
            put("type", "resize")
        }

        channel.port2.postMessage(JSON.parse(json.encodeToString(messageWithType)))

        advanceUntilIdle()

        resizeCompleted.await()

        verifySuspend { mockIframeContainerResizer.resize(DISMISS_ID.toIframeId(), testHeight) }
        verifySuspend(VerifyMode.exactly(0)) {
            mockEventActionFactory.create(any())
        }
    }

    @Test
    fun provide_shouldExecuteActionsSequentially_dismissWaitsForPrecedingAction() = runTest {
        val executionOrder = mutableListOf<String>()
        val customEventGate = CompletableDeferred<Unit>()
        val dismissCompleted = CompletableDeferred<Unit>()

        val customEventActionModel = BasicCustomEventActionModel(
            REPORTING,
            "testEvent",
            mapOf("pay" to "load")
        )
        val dismissActionModel = BasicDismissActionModel(dismissId = DISMISS_ID, inAppType = InAppType.OVERLAY)

        val mockCustomEventAction: Action<*> = mock(MockMode.autofill) {
            everySuspend { invoke() } calls {
                customEventGate.await()
                executionOrder.add("customEvent")
            }
        }
        val mockDismissAction: Action<*> = mock(MockMode.autofill) {
            everySuspend { invoke() } calls {
                executionOrder.add("dismiss")
                dismissCompleted.complete(Unit)
            }
        }

        everySuspend { mockEventActionFactory.create(customEventActionModel) } returns mockCustomEventAction
        everySuspend { mockEventActionFactory.create(dismissActionModel) } returns mockDismissAction

        val channel = messageChannelProvider.provide(testInAppMessage)

        val customEventMessage = buildJsonObject {
            put("type", "MECustomEvent")
            put("reporting", REPORTING)
            put("name", "testEvent")
            put("payload", json.encodeToJsonElement(mapOf("pay" to "load")))
        }
        val dismissMessage = buildJsonObject {
            put("type", "Dismiss")
        }

        channel.port2.postMessage(JSON.parse(json.encodeToString(customEventMessage)))
        channel.port2.postMessage(JSON.parse(json.encodeToString(dismissMessage)))

        advanceUntilIdle()

        customEventGate.complete(Unit)
        advanceUntilIdle()

        dismissCompleted.await()

        assertEquals(listOf("customEvent", "dismiss"), executionOrder, "Actions should execute sequentially in order")
    }
}