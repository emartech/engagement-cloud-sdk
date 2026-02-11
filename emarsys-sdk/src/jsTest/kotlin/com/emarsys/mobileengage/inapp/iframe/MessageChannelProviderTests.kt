package com.emarsys.mobileengage.inapp.iframe

import com.emarsys.core.log.Logger
import com.emarsys.mobileengage.action.EventActionFactoryApi
import com.emarsys.mobileengage.action.actions.Action
import com.emarsys.mobileengage.action.models.BasicDismissActionModel
import com.emarsys.mobileengage.action.models.BasicInAppButtonClickedActionModel
import com.emarsys.mobileengage.action.models.BasicOpenExternalUrlActionModel
import com.emarsys.mobileengage.action.models.HtmlTarget
import com.emarsys.mobileengage.inapp.InAppMessage
import com.emarsys.mobileengage.inapp.toIframeId
import com.emarsys.util.JsonUtil
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
import kotlinx.serialization.json.put
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

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
        val amendedActionModel = testActionModel.copy(dismissId = DISMISS_ID)
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
            ""
        )
        val amendedActionModel = testActionModel.copy(trackingInfo = TRACKING_INFO)
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
}