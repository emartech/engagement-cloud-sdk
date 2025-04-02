package com.emarsys.mobileengage.push

import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import com.emarsys.api.push.PushConstants.DEFAULT_TAP_ACTION_ID
import com.emarsys.api.push.PushConstants.INTENT_EXTRA_ACTION_KEY
import com.emarsys.api.push.PushConstants.INTENT_EXTRA_DEFAULT_TAP_ACTION_KEY
import com.emarsys.api.push.PushConstants.INTENT_EXTRA_PAYLOAD_KEY
import com.emarsys.core.actions.ActionHandlerApi
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.log.Logger
import com.emarsys.mobileengage.action.PushActionFactoryApi
import com.emarsys.mobileengage.action.actions.Action
import com.emarsys.mobileengage.action.actions.DismissAction
import com.emarsys.mobileengage.action.actions.LaunchApplicationAction
import com.emarsys.mobileengage.action.actions.ReportingAction
import com.emarsys.mobileengage.action.models.BasicAppEventActionModel
import com.emarsys.mobileengage.action.models.BasicDismissActionModel
import com.emarsys.mobileengage.action.models.BasicLaunchApplicationActionModel
import com.emarsys.mobileengage.action.models.BasicPushButtonClickedActionModel
import com.emarsys.mobileengage.action.models.NotificationOpenedActionModel
import com.emarsys.mobileengage.action.models.PresentableActionModel
import com.emarsys.mobileengage.action.models.PresentableAppEventActionModel
import com.emarsys.mobileengage.action.models.PresentableDismissActionModel
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
        const val COLLAPSE_ID = "testCollapseId"
        val PAYLOAD = mapOf("testKey" to "testValue")
        const val APP_EVENT_ACTION_MODEL_JSON =
            """{"type":"MEAppEvent", "id":"$ID","title":"$TITLE","name":"$NAME","payload":{"testKey":"testValue"}}"""
    }

    private val json = JsonUtil.json
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var testDispatcher: TestDispatcher
    private lateinit var testScope: CoroutineScope
    private lateinit var mockActionFactory: PushActionFactoryApi
    private lateinit var mockActionHandler: ActionHandlerApi
    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi
    private lateinit var mockLogger: Logger
    private lateinit var notificationIntentProcessor: NotificationIntentProcessor

    @Before
    fun setUp() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        testScope = CoroutineScope(testDispatcher)
        mockActionFactory = mockk(relaxed = true)
        mockActionHandler = mockk(relaxed = true)
        mockSdkEventDistributor = mockk(relaxed = true)
        mockLogger = mockk(relaxed = true)
        notificationIntentProcessor =
            NotificationIntentProcessor(json, mockActionFactory, mockActionHandler, mockLogger)
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
        val basicDismissActionModel = BasicDismissActionModel(COLLAPSE_ID)
        val reportingAction = ReportingAction(buttonClickedActionModel, mockSdkEventDistributor)
        val mockLaunchApplicationAction: LaunchApplicationAction = mockk(relaxed = true)
        val dismissAction = DismissAction(basicDismissActionModel, mockSdkEventDistributor)

        coEvery { mockActionFactory.create(BasicLaunchApplicationActionModel) } returns mockLaunchApplicationAction
        coEvery { mockActionFactory.create(buttonClickedActionModel) } returns reportingAction
        coEvery { mockActionFactory.create(basicDismissActionModel) } returns dismissAction

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
                    dismissAction,
                    reportingAction,
                ), mockAction
            )
        }
    }

    @Test
    fun testProcessIntent_shouldNotCrash_whenIntentContains_invalidJsonString() = runTest {
        val actionModel = PresentableAppEventActionModel(ID, TITLE, NAME, PAYLOAD)

        val intent = createTestIntent(actionModel)

        intent.putExtra(INTENT_EXTRA_PAYLOAD_KEY, """{"missing":"keys"}""")

        notificationIntentProcessor.processIntent(intent, testScope)

        advanceUntilIdle()
    }

    @Test
    fun testProcessIntent_shouldNotIncludeLaunchApplication_andBasicDismissAction_InMandatoryActions_whenActionIsDismiss() =
        runTest {
            val dismissId = "collapseId"
            val dismissActionModel = PresentableDismissActionModel(ID, TITLE, dismissId)
            val dismissActionModelJson = """{
                    "type":"Dismiss",
                    "id":"$ID",
                    "title":"$TITLE",
                    "dismissId":"$dismissId"
                }""".trimIndent()
            val buttonClickedActionModel = BasicPushButtonClickedActionModel(ID, SID)
            val reportingAction = ReportingAction(buttonClickedActionModel, mockSdkEventDistributor)

            coEvery { mockActionFactory.create(buttonClickedActionModel) } returns reportingAction

            val intent = createTestIntent(dismissActionModel, dismissActionModelJson)

            intent.putExtra(INTENT_EXTRA_PAYLOAD_KEY, createTestMessage(dismissActionModelJson))

            val mockAction: Action<Unit> = mockk(relaxed = true)
            coEvery { mockActionFactory.create(dismissActionModel) } returns mockAction

            notificationIntentProcessor.processIntent(intent, testScope)

            advanceUntilIdle()

            coVerify { mockActionFactory.create(dismissActionModel) }
            coVerify {
                mockActionHandler.handleActions(
                    listOf(
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
            val basicDismissActionModel = BasicDismissActionModel(COLLAPSE_ID)
            val reportingAction = ReportingAction(notificationOpenedActionModel, mockSdkEventDistributor)
            val basicDismissAction = DismissAction(basicDismissActionModel, mockSdkEventDistributor)
            val mockLaunchApplicationAction: LaunchApplicationAction = mockk(relaxed = true)

            coEvery { mockActionFactory.create(BasicLaunchApplicationActionModel) } returns mockLaunchApplicationAction
            coEvery { mockActionFactory.create(notificationOpenedActionModel) } returns reportingAction
            coEvery { mockActionFactory.create(basicDismissActionModel) } returns basicDismissAction

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
                        basicDismissAction,
                        reportingAction,
                    ), mockAction
                )
            }
        }

    private fun createTestIntent(
        actionModel: PresentableActionModel,
        actionModelJson: String = APP_EVENT_ACTION_MODEL_JSON
    ): Intent {
        val intent = Intent(context, NotificationOpenedActivity::class.java)
        intent.action = actionModel.id
        intent.putExtra(
            INTENT_EXTRA_ACTION_KEY,
            actionModelJson
        )
        return intent
    }

    private fun createTestMessage(
        actionModelString: String = """{
                    "type":"MEAppEvent",
                    "id":"$ID",
                    "title":"$TITLE",
                    "name":"$NAME",
                    "payload":{"testKey":"testValue"}
                }""".trimIndent()
    ): String {
        return """{
            "sid":"$SID",
            "campaignId":"testCampaignId",
            "platformData":{
                "channelId":"testChannelId",
                "notificationMethod":{
                    "collapseId":"$COLLAPSE_ID",
                    "operation":"${NotificationOperation.INIT}"
                }
            },
            "displayableData":{
                "title": "testTitle",
                "body": "testBody",
            },
            "actionableData": {
                "actions":
                     [
                        $actionModelString
                    ],
            }
        }""".trimIndent()
    }
}