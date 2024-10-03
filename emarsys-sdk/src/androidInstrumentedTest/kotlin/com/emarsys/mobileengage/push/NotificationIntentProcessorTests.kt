package com.emarsys.mobileengage.push

import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import com.emarsys.api.push.PushConstants.INTENT_EXTRA_ACTION_KEY
import com.emarsys.api.push.PushConstants.INTENT_EXTRA_PAYLOAD_KEY
import com.emarsys.core.actions.ActionHandlerApi
import com.emarsys.core.channel.CustomEventChannelApi
import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.actions.Action
import com.emarsys.mobileengage.action.actions.ButtonClickedAction
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.action.models.BasicPushButtonClickedActionModel
import com.emarsys.mobileengage.action.models.PresentableAppEventActionModel
import com.emarsys.mobileengage.push.model.NotificationOperation
import com.emarsys.util.JsonUtil
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    private lateinit var mockEventChannel: CustomEventChannelApi
    private lateinit var notificationIntentProcessor: NotificationIntentProcessor

    @Before
    fun setUp() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        testScope = CoroutineScope(testDispatcher)
        mockActionFactory = mockk(relaxed = true)
        mockActionHandler = mockk(relaxed = true)
        mockEventChannel = mockk(relaxed = true)
        notificationIntentProcessor =
            NotificationIntentProcessor(json, mockActionFactory, mockActionHandler, mockEventChannel)
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
        val buttonClickedAction = ButtonClickedAction(buttonClickedActionModel, mockEventChannel)

        val intent = createTestIntent(actionModel)

        intent.putExtra(INTENT_EXTRA_PAYLOAD_KEY, createTestMessage())

        val mockAction: Action<Unit> = mockk(relaxed = true)
        coEvery { mockActionFactory.create(actionModel) } returns mockAction

        notificationIntentProcessor.processIntent(intent, testScope)

        advanceUntilIdle()

        coVerify { mockActionFactory.create(actionModel) }
        coVerify { mockActionHandler.handleActions(listOf(buttonClickedAction), mockAction) }
    }

    private fun createTestIntent(actionModel: PresentableAppEventActionModel): Intent {
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