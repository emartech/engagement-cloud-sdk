package com.emarsys.mobileengage.push

import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import com.emarsys.api.push.PushConstants.DEFAULT_TAP_ACTION_ID
import com.emarsys.api.push.PushConstants.INTENT_EXTRA_ACTION_KEY
import com.emarsys.api.push.PushConstants.INTENT_EXTRA_DEFAULT_TAP_ACTION_KEY
import com.emarsys.api.push.PushConstants.INTENT_EXTRA_PAYLOAD_KEY
import com.emarsys.core.actions.ActionHandlerApi
import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.actions.Action
import com.emarsys.mobileengage.action.actions.LaunchApplicationAction
import com.emarsys.mobileengage.action.actions.ReportingAction
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.action.models.BasicAppEventActionModel
import com.emarsys.mobileengage.action.models.BasicLaunchApplicationActionModel
import com.emarsys.mobileengage.action.models.BasicPushButtonClickedActionModel
import com.emarsys.mobileengage.action.models.NotificationOpenedActionModel
import com.emarsys.mobileengage.action.models.PresentableActionModel
import com.emarsys.mobileengage.action.models.PresentableAppEventActionModel
import com.emarsys.mobileengage.push.model.NotificationOperation
import com.emarsys.networking.clients.event.model.SdkEvent
import com.emarsys.util.JsonUtil
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationIntentProcessorTests {
    private companion object {
        const val ID = "testId"
        const val TITLE = "testTitle"
        const val NAME = "testName"
        const val SID = "testSid"
        val PAYLOAD = mapOf("testKey" to "testValue")
        const val ACTION_MODEL_JSON =
            """{"type":"MEAppEvent", "id":"$ID","title":"$TITLE","name":"$NAME","payload":{"testKey":"testValue"}}"""
    }

    private val json = JsonUtil.json
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var testDispatcher: TestDispatcher
    private lateinit var testScope: CoroutineScope
    private lateinit var mockActionFactory: ActionFactoryApi<ActionModel>
    private lateinit var mockActionHandler: ActionHandlerApi
    private lateinit var mockSdkEventFlow: MutableSharedFlow<SdkEvent>
    private lateinit var notificationIntentProcessor: NotificationIntentProcessor

    @Before
    fun setUp() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        testScope = CoroutineScope(testDispatcher)
        mockActionFactory = mockk(relaxed = true)
        mockActionHandler = mockk(relaxed = true)
        mockSdkEventFlow = mockk(relaxed = true)
        notificationIntentProcessor =
            NotificationIntentProcessor(json, mockActionFactory, mockActionHandler)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testProcessIntent_shouldHandleAction_withActionHandler() = runTest {
        val actionModel = PresentableAppEventActionModel(ID, TITLE, NAME, PAYLOAD)
        val intent = createTestIntent(actionModel)
        val mockAction: Action<Unit> = mockk(relaxed = true)
        coEvery { mockActionFactory.create(actionModel) } returns mockAction

        notificationIntentProcessor.processIntent(intent, testScope)

        advanceUntilIdle()

        coVerify { mockActionFactory.create(actionModel) }
        coVerify { mockActionHandler.handleActions(any(), mockAction) }
    }

    @Test
    fun testProcessIntent_shouldHandleAction_withActionHandler_withMandatoryActions() = runTest {
        val actionModel = PresentableAppEventActionModel(ID, TITLE, NAME, PAYLOAD)
        val buttonClickedActionModel = BasicPushButtonClickedActionModel(ID, SID)
        val reportingAction = ReportingAction(buttonClickedActionModel, mockSdkEventFlow)
        val mockLaunchApplicationAction: LaunchApplicationAction = mockk(relaxed = true)

        coEvery { mockActionFactory.create(BasicLaunchApplicationActionModel) } returns mockLaunchApplicationAction
        coEvery { mockActionFactory.create(buttonClickedActionModel) } returns reportingAction

        val intent = createTestIntent(actionModel)

        intent.putExtra(INTENT_EXTRA_PAYLOAD_KEY, createTestMessage())

        val mockAction: Action<Unit> = mockk(relaxed = true)
        coEvery { mockActionFactory.create(actionModel) } returns mockAction

        notificationIntentProcessor.processIntent(intent, testScope)

        advanceUntilIdle()

        coVerify { mockActionFactory.create(actionModel) }
        coVerify {
            mockActionHandler.handleActions(
                listOf(
                    mockLaunchApplicationAction,
                    reportingAction
                ), mockAction
            )
        }
    }

    @Test
    fun testProcessIntent_shouldHandleAction_withActionHandler_withMandatoryActions_whenTriggeredWithDefaultAction() =
        runTest {
            val actionModel = BasicAppEventActionModel(NAME, PAYLOAD)
            val actionJsonString =
                """{"type": "MEAppEvent", "name":"$NAME","payload":{"testKey":"testValue"}}"""
            val notificationOpenedActionModel = NotificationOpenedActionModel(SID)
            val reportingAction = ReportingAction(notificationOpenedActionModel, mockSdkEventFlow)
            val mockLaunchApplicationAction: LaunchApplicationAction = mockk(relaxed = true)

            coEvery { mockActionFactory.create(BasicLaunchApplicationActionModel) } returns mockLaunchApplicationAction
            coEvery { mockActionFactory.create(notificationOpenedActionModel) } returns reportingAction

            val intent = Intent(context, NotificationOpenedActivity::class.java)
            intent.action = DEFAULT_TAP_ACTION_ID
            intent.putExtra(
                INTENT_EXTRA_DEFAULT_TAP_ACTION_KEY,
                actionJsonString
            )
            intent.putExtra(INTENT_EXTRA_PAYLOAD_KEY, createTestMessage())

            val mockAction: Action<Unit> = mockk(relaxed = true)
            coEvery { mockActionFactory.create(actionModel) } returns mockAction

            notificationIntentProcessor.processIntent(intent, testScope)

            advanceUntilIdle()

            coVerify { mockActionFactory.create(actionModel) }
            coVerify {
                mockActionHandler.handleActions(
                    listOf(
                        mockLaunchApplicationAction,
                        reportingAction
                    ), mockAction
                )
            }
        }

    private fun createTestIntent(actionModel: PresentableActionModel): Intent {
        val intent = Intent(context, NotificationOpenedActivity::class.java)
        intent.action = actionModel.id
        intent.putExtra(
            INTENT_EXTRA_ACTION_KEY,
            ACTION_MODEL_JSON
        )
        return intent
    }

    private fun createTestMessage(): String {
        return """{
        "messageId":"testMessageId",
        "title": "testTitle",
        "body": "testBody",
        "data":{
            "sid":"$SID",
            "campaignId":"testCampaignId",
            "platformData":{
                "channelId":"testChannelId",
                "notificationMethod":{
                    "collapseId":"testCollapseId",
                    "operation":"${NotificationOperation.INIT}"
                    }
                }
            }
            "actions": [
                {
                    "type":"MEAppEvent",
                    "id":"$ID",
                    "title":"$TITLE",
                    "name":"$NAME",
                    "payload":{"testKey":"testValue"}
                }
            ]
        }""".trimIndent()
    }
}