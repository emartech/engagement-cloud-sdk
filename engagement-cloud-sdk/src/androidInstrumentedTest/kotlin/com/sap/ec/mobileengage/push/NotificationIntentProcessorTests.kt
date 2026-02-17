package com.sap.ec.mobileengage.push

import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import com.sap.ec.api.push.PushConstants.DEFAULT_TAP_ACTION_ID
import com.sap.ec.api.push.PushConstants.INTENT_EXTRA_ACTION_KEY
import com.sap.ec.api.push.PushConstants.INTENT_EXTRA_DEFAULT_TAP_ACTION_KEY
import com.sap.ec.api.push.PushConstants.INTENT_EXTRA_PAYLOAD_KEY
import com.sap.ec.core.actions.ActionHandlerApi
import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.log.Logger
import com.sap.ec.mobileengage.action.PushActionFactoryApi
import com.sap.ec.mobileengage.action.actions.Action
import com.sap.ec.mobileengage.action.actions.DismissAction
import com.sap.ec.mobileengage.action.actions.LaunchApplicationAction
import com.sap.ec.mobileengage.action.actions.ReportingAction
import com.sap.ec.mobileengage.action.models.BasicAppEventActionModel
import com.sap.ec.mobileengage.action.models.BasicDismissActionModel
import com.sap.ec.mobileengage.action.models.BasicLaunchApplicationActionModel
import com.sap.ec.mobileengage.action.models.BasicPushButtonClickedActionModel
import com.sap.ec.mobileengage.action.models.BasicPushToInAppActionModel
import com.sap.ec.mobileengage.action.models.NotificationOpenedActionModel
import com.sap.ec.mobileengage.action.models.PresentableActionModel
import com.sap.ec.mobileengage.action.models.PresentableAppEventActionModel
import com.sap.ec.mobileengage.action.models.PresentableDismissActionModel
import com.sap.ec.mobileengage.inapp.networking.models.PushToInAppPayload
import com.sap.ec.util.JsonUtil
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationIntentProcessorTests {
    private companion object {
        val PAYLOAD = mapOf("testKey" to "testValue")
        const val REPORTING = "{\"someKey\":\"someValue\"}"
        const val TITLE = "testTitle"
        const val ID = "testId"
        const val NAME = "testName"
        const val COLLAPSE_ID = "testCollapseId"
        const val TRACKING_INFO = """{"trackingInfoKey":"trackingInfoValue"}"""
        const val APP_EVENT_ACTION_MODEL_JSON =
            """{"type":"MEAppEvent","id":"$ID","reporting":"{\"someKey\":\"someValue\"}","title":"$TITLE","name":"$NAME","payload":{"testKey":"testValue"}}"""
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
        Dispatchers.setMain(StandardTestDispatcher())
        testDispatcher = StandardTestDispatcher()
        testScope = CoroutineScope(testDispatcher)
        mockActionFactory = mockk(relaxed = true)
        mockActionHandler = mockk(relaxed = true)
        mockSdkEventDistributor = mockk(relaxed = true)
        mockLogger = mockk(relaxed = true)
        notificationIntentProcessor =
            NotificationIntentProcessor(
                json,
                mockActionFactory,
                mockActionHandler,
                testScope,
                mockLogger
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testProcessIntent_shouldHandleAction_withActionHandler() = runTest {
        val actionModel = PresentableAppEventActionModel(
            id = ID,
            reporting = REPORTING,
            title = TITLE,
            name = NAME,
            payload = PAYLOAD
        )
        val intent = createTestIntent(actionModel)
        val mockAction: Action<Unit> = mockk(relaxed = true)
        coEvery { mockActionFactory.create(actionModel) } returns mockAction

        notificationIntentProcessor.processIntent(intent)

        advanceUntilIdle()

        coVerify { mockActionFactory.create(actionModel) }
        coVerify { mockActionHandler.handleActions(any(), mockAction) }
    }

    @Test
    fun testProcessIntent_shouldHandleMandatoryActions_withActionHandler_whenThereWasNoTriggeredAction() =
        runTest {
            val intent = Intent()
            intent.putExtra(INTENT_EXTRA_PAYLOAD_KEY, createTestMessage())
            val mockLaunchApplicationAction = mockk<LaunchApplicationAction>(relaxed = true)
            val mockBasicDismissAction = mockk<DismissAction>(relaxed = true)
            val mockReportingAction = mockk<DismissAction>(relaxed = true)
            coEvery { mockActionFactory.create(BasicLaunchApplicationActionModel) } returns mockLaunchApplicationAction
            coEvery { mockActionFactory.create(BasicDismissActionModel(COLLAPSE_ID)) } returns mockBasicDismissAction
            coEvery {
                mockActionFactory.create(
                    NotificationOpenedActionModel(
                        null,
                        TRACKING_INFO
                    )
                )
            } returns mockReportingAction
            val expectedMandatoryActions =
                listOf(
                    mockLaunchApplicationAction,
                    mockBasicDismissAction,
                    mockReportingAction
                )

            notificationIntentProcessor.processIntent(intent)

            advanceUntilIdle()

            coVerify { mockActionHandler.handleActions(expectedMandatoryActions, null) }
        }

    @Test
    fun testProcessIntent_shouldHandleAction_withActionHandler_withMandatoryActions() = runTest {
        val actionModel = PresentableAppEventActionModel(
            id = ID,
            reporting = REPORTING,
            title = TITLE,
            name = NAME,
            payload = PAYLOAD
        )
        val buttonClickedActionModel = BasicPushButtonClickedActionModel(REPORTING, TRACKING_INFO)
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

        notificationIntentProcessor.processIntent(intent)

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
    fun testProcessIntent_shouldNotCrash_whenIntentContains_invalidJsonString_forThePushMessage() =
        runTest {
            val actionModel = PresentableAppEventActionModel(
                id = ID,
                reporting = REPORTING,
                title = TITLE,
                name = NAME,
                payload = PAYLOAD
            )

            val intent = createTestIntent(actionModel)

            intent.putExtra(INTENT_EXTRA_PAYLOAD_KEY, """{"missing":"keys"}""")

            notificationIntentProcessor.processIntent(intent)

            advanceUntilIdle()
        }

    @Test
    fun testProcessIntent_shouldNotCrash_whenIntentContains_invalidJsonString_forTriggeredAction() =
        runTest {
            val actionModel = PresentableAppEventActionModel(
                id = ID,
                reporting = REPORTING,
                title = TITLE,
                name = NAME,
                payload = PAYLOAD
            )

            val intent = createTestIntent(actionModel)

            intent.putExtra(INTENT_EXTRA_ACTION_KEY, """{"missing":"keys"}""")

            notificationIntentProcessor.processIntent(intent)

            advanceUntilIdle()
        }

    @Test
    fun testProcessIntent_shouldNotCrash_whenIntentContains_invalidJsonString_forDefaultAction() =
        runTest {
            val intent = Intent(context, NotificationOpenedActivity::class.java)

            intent.putExtra(INTENT_EXTRA_DEFAULT_TAP_ACTION_KEY, """{"missing":"keys"}""")

            notificationIntentProcessor.processIntent(intent)

            advanceUntilIdle()
        }

    @Test
    fun testProcessIntent_shouldNotIncludeLaunchApplication_andBasicDismissAction_InMandatoryActions_whenActionIsDismiss() =
        runTest {
            val dismissId = "collapseId"
            val dismissActionModel = PresentableDismissActionModel(ID, REPORTING, TITLE, dismissId)
            val dismissActionModelJson = """{
                    "type":"Dismiss",
                    "id":$ID,
                    "reporting":"{\"someKey\":\"someValue\"}",
                    "title":"$TITLE",
                    "dismissId":"$dismissId"
                }""".trimIndent()
            val buttonClickedActionModel =
                BasicPushButtonClickedActionModel(REPORTING, TRACKING_INFO)
            val reportingAction = ReportingAction(buttonClickedActionModel, mockSdkEventDistributor)

            coEvery { mockActionFactory.create(buttonClickedActionModel) } returns reportingAction

            val intent = createTestIntent(dismissActionModel, dismissActionModelJson)

            intent.putExtra(INTENT_EXTRA_PAYLOAD_KEY, createTestMessage(dismissActionModelJson))

            val mockAction: Action<Unit> = mockk(relaxed = true)
            coEvery { mockActionFactory.create(dismissActionModel) } returns mockAction

            notificationIntentProcessor.processIntent(intent)

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
            val actionModel = BasicAppEventActionModel(REPORTING, NAME, PAYLOAD)
            val actionJsonString =
                """{"type": "MEAppEvent","reporting":"{\"someKey\":\"someValue\"}","name":"$NAME","payload":{"testKey":"testValue"}}"""
            val notificationOpenedActionModel =
                NotificationOpenedActionModel(REPORTING, TRACKING_INFO)
            val basicDismissActionModel = BasicDismissActionModel(COLLAPSE_ID)
            val reportingAction =
                ReportingAction(notificationOpenedActionModel, mockSdkEventDistributor)
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

            notificationIntentProcessor.processIntent(intent)

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

    @Test
    fun testProcessIntent_shouldHandleAction_withActionHandler_withMandatoryActions_whenActionIsPushToInApp() =
        runTest {
            val actionModelSlot = slot<BasicPushToInAppActionModel>()
            val actionJsonString =
                """{"type": "InApp","reporting":"{\"someKey\":\"someValue\"}","payload":{"url":"https://www.sap.com"}}"""
            val notificationOpenedActionModel =
                NotificationOpenedActionModel(REPORTING, TRACKING_INFO)
            val basicDismissActionModel = BasicDismissActionModel(COLLAPSE_ID)
            val reportingAction =
                ReportingAction(notificationOpenedActionModel, mockSdkEventDistributor)
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
            coEvery { mockActionFactory.create(capture(actionModelSlot)) } returns mockAction

            notificationIntentProcessor.processIntent(intent)

            advanceUntilIdle()

            actionModelSlot.captured.payload shouldBe PushToInAppPayload(url = "https://www.sap.com")
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
        intent.action = actionModel.reporting
        intent.putExtra(
            INTENT_EXTRA_ACTION_KEY,
            actionModelJson
        )
        return intent
    }

    private fun createTestMessage(
        actionModelString: String = """{
                    "type":"MEAppEvent",
                    "reporting":"$REPORTING",
                    "title":"$TITLE",
                    "name":"$NAME",
                    "payload":{"testKey":"testValue"}
                }""".trimIndent()
    ): String {
        val jsonMessage = buildJsonObject {
            put("trackingInfo", """{"trackingInfoKey":"trackingInfoValue"}""")
            put("platformData", buildJsonObject {
                put("channelId", "testChannelId")
                put("notificationMethod", buildJsonObject {
                    put("collapseId", COLLAPSE_ID)
                    put("operation", NotificationOperation.INIT.name)
                })
            })
            put("displayableData", buildJsonObject {
                put("title", TITLE)
                put("body", "testBody")
            })
            put("actionableData", buildJsonObject {
                put(
                    "actions",
                    buildJsonArray {
                        buildJsonObject {
                            put(
                                "actionModelString",
                                actionModelString
                            )
                        }
                    })
            })
        }
        return json.encodeToString(jsonMessage)
    }
}