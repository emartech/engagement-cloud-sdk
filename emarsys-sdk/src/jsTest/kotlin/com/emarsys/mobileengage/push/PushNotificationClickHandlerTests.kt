package com.emarsys.mobileengage.push

import com.emarsys.core.actions.ActionHandlerApi
import com.emarsys.mobileengage.action.PushActionFactoryApi
import com.emarsys.mobileengage.action.actions.Action
import com.emarsys.mobileengage.action.actions.ReportingAction
import com.emarsys.mobileengage.action.models.BasicActionModel
import com.emarsys.mobileengage.action.models.BasicOpenExternalUrlActionModel
import com.emarsys.mobileengage.action.models.BasicPushButtonClickedActionModel
import com.emarsys.mobileengage.action.models.BasicPushToInAppActionModel
import com.emarsys.mobileengage.action.models.NotificationOpenedActionModel
import com.emarsys.mobileengage.action.models.PresentableActionModel
import com.emarsys.mobileengage.action.models.PresentableOpenExternalUrlActionModel
import com.emarsys.mobileengage.inapp.PushToInAppPayload
import com.emarsys.mobileengage.push.model.JsNotificationClickedData
import com.emarsys.mobileengage.push.model.JsPlatformData
import com.emarsys.mobileengage.push.model.JsPushMessage
import com.emarsys.util.JsonUtil
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture.Companion.slot
import dev.mokkery.matcher.capture.capture
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifyNoMoreCalls
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import web.broadcast.BroadcastChannel
import kotlin.test.BeforeTest
import kotlin.test.Test

class PushNotificationClickHandlerTests {
    private companion object {
        const val TRACKING_INFO = """{"trackingInfoKey":"trackingInfoValue"}"""
        const val REPORTING = """{"reportingKey":"reportingValue"}"""
    }

    private lateinit var mockActionFactory: PushActionFactoryApi
    private lateinit var mockAction: Action<*>
    private lateinit var mockDefaultTapAction: Action<*>
    private lateinit var mockButtonClickedAction: Action<*>
    private lateinit var mockActionHandler: ActionHandlerApi
    private lateinit var onNotificationClickedBroadcastChannel: BroadcastChannel
    private lateinit var pushNotificationClickHandler: PushNotificationClickHandler

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeTest
    fun setup() = runTest {
        mockActionFactory = mock()
        mockAction = mock(MockMode.autofill)
        mockDefaultTapAction = mock(MockMode.autofill)
        mockButtonClickedAction = mock(MockMode.autofill)
        mockActionHandler = mock<ActionHandlerApi>()

        onNotificationClickedBroadcastChannel =
            BroadcastChannel("emarsys-sdk-on-notification-clicked-channel")
        Dispatchers.setMain(StandardTestDispatcher())
        pushNotificationClickHandler =
            PushNotificationClickHandler(
                mockActionFactory,
                mockActionHandler,
                onNotificationClickedBroadcastChannel,
                TestScope(),
                sdkLogger = mock {
                    everySuspend {
                        error(
                            any<String>(),
                            any<Throwable>()
                        )
                    } returns Unit
                }
            )
    }

    @Test
    fun register_shouldRegisterOnMessageListener_onOnNotificationClickedBroadcastChannel() =
        runTest {
            pushNotificationClickHandler.register()

            onNotificationClickedBroadcastChannel.onmessage shouldNotBe null
        }

    @Test
    fun handleNotificationClick_shouldHandleDefaultTapAction_andNotHandlePushButtonClickedAction_whenDefaultTapAction_isPresent() =
        runTest {
            val defaultTapActionModel =
                BasicOpenExternalUrlActionModel(REPORTING, "https://www.google.com")
            val notificationClickedData =
                createTestJsNotificationClickedData(
                    "",
                    defaultTapActionModel = defaultTapActionModel
                )
            val event =
                JsonUtil.json.encodeToString<JsNotificationClickedData>(notificationClickedData)
            val notificationOpenedActionModel =
                NotificationOpenedActionModel(REPORTING, TRACKING_INFO)
            val expectedReportingAction = ReportingAction(notificationOpenedActionModel, mock())
            everySuspend { mockActionFactory.create(defaultTapActionModel) } returns mockDefaultTapAction
            everySuspend { mockActionFactory.create(notificationOpenedActionModel) } returns expectedReportingAction

            pushNotificationClickHandler.handleNotificationClick(event)

            verifySuspend { mockActionFactory.create(defaultTapActionModel) }
            verifySuspend {
                mockActionHandler.handleActions(
                    listOf(expectedReportingAction),
                    mockDefaultTapAction
                )
            }
        }

    @Test
    fun handleNotificationClick_shouldAddReportingAction_forNotificationOpened_whenDefaultTapAction_wasTriggering() =
        runTest {
            val defaultTapActionModel =
                BasicOpenExternalUrlActionModel(REPORTING, "https://www.google.com")
            val notificationClickedData =
                createTestJsNotificationClickedData(
                    "",
                    defaultTapActionModel = defaultTapActionModel
                )
            val event =
                JsonUtil.json.encodeToString<JsNotificationClickedData>(notificationClickedData)
            val notificationOpenedActionModel =
                NotificationOpenedActionModel(REPORTING, TRACKING_INFO)
            val expectedReportingAction = ReportingAction(notificationOpenedActionModel, mock())
            everySuspend { mockActionFactory.create(defaultTapActionModel) } returns mockDefaultTapAction
            everySuspend { mockActionFactory.create(notificationOpenedActionModel) } returns expectedReportingAction

            pushNotificationClickHandler.handleNotificationClick(event)

            verifySuspend { mockActionFactory.create(defaultTapActionModel) }
            verifySuspend {
                mockActionHandler.handleActions(
                    listOf(expectedReportingAction),
                    mockDefaultTapAction
                )
            }
        }

    @Test
    fun handleNotificationClick_shouldHandleActionAndMandatoryAction_whenActionWithId_isPresent() =
        runTest {
            val actionId = "testActionId"
            val actionModel =
                PresentableOpenExternalUrlActionModel(
                    actionId,
                    REPORTING,
                    "title",
                    "https://www.google.com"
                )
            val notificationClickedData =
                createTestJsNotificationClickedData(actionId, actionModels = listOf(actionModel))
            val event =
                JsonUtil.json.encodeToString<JsNotificationClickedData>(notificationClickedData)
            everySuspend { mockActionFactory.create(actionModel) } returns mockAction
            everySuspend {
                mockActionFactory.create(
                    BasicPushButtonClickedActionModel(
                        REPORTING,
                        notificationClickedData.jsPushMessage.trackingInfo
                    )
                )
            } returns mockButtonClickedAction

            pushNotificationClickHandler.handleNotificationClick(event)

            verifySuspend {
                mockActionHandler.handleActions(
                    listOf(mockButtonClickedAction),
                    mockAction
                )
            }
        }

    @Test
    fun handleNotificationClick_shouldHandle_pushToInAppActionAndMandatoryAction() =
        runTest {
            val testPushToInAppPayload = PushToInAppPayload("https://www.sap.com")
            val actionId = ""
            val actionModel =
                BasicPushToInAppActionModel(
                    reporting = REPORTING,
                    payload = testPushToInAppPayload
                )
            val notificationClickedData =
                createTestJsNotificationClickedData(actionId, defaultTapActionModel = actionModel)
            val event =
                JsonUtil.json.encodeToString<JsNotificationClickedData>(notificationClickedData)
            val actionModelSlot = slot<BasicPushToInAppActionModel>()
            val mockNotificationOpenedAction: Action<*> = mock(MockMode.autofill)
            everySuspend { mockActionFactory.create(capture(actionModelSlot)) } returns mockAction
            everySuspend {
                mockActionFactory.create(
                    NotificationOpenedActionModel(
                        REPORTING,
                        notificationClickedData.jsPushMessage.trackingInfo
                    )
                )
            } returns mockNotificationOpenedAction

            pushNotificationClickHandler.handleNotificationClick(event)

            actionModelSlot.values.first().payload shouldBe testPushToInAppPayload
            verifySuspend {
                mockActionHandler.handleActions(
                    listOf(mockNotificationOpenedAction),
                    mockAction
                )
            }
        }

    @Test
    fun handleNotificationClick_shouldStillExecuteReportingAction_whenActionWithId_isNotPresent() =
        runTest {
            val mockReportingAction = mock<Action<*>>()
            everySuspend {
                mockActionFactory.create(
                    NotificationOpenedActionModel(
                        null,
                        TRACKING_INFO
                    )
                )
            } returns mockReportingAction
            val actionModel =
                PresentableOpenExternalUrlActionModel(
                    "actionId",
                    REPORTING,
                    "title",
                    "https://www.google.com"
                )
            val notificationClickedData =
                createTestJsNotificationClickedData(
                    "differentClickedActionId",
                    actionModels = listOf(actionModel)
                )
            val event =
                JsonUtil.json.encodeToString<JsNotificationClickedData>(notificationClickedData)

            pushNotificationClickHandler.handleNotificationClick(event)

            verifySuspend {
                mockActionFactory.create(
                    NotificationOpenedActionModel(
                        null,
                        TRACKING_INFO
                    )
                )
            }
            verifySuspend { mockActionHandler.handleActions(listOf(mockReportingAction), null) }
        }

    @Test
    fun handleNotificationClick_shouldReportOpen_whenNeitherDefaultTapAction_norAnyOtherAction_arePresent() =
        runTest {
            val mockReportingAction = mock<Action<*>>()
            everySuspend {
                mockActionFactory.create(
                    NotificationOpenedActionModel(
                        null,
                        TRACKING_INFO
                    )
                )
            } returns mockReportingAction
            val notificationClickedData =
                createTestJsNotificationClickedData(
                    "",
                    defaultTapActionModel = null,
                    actionModels = null
                )
            val event =
                JsonUtil.json.encodeToString<JsNotificationClickedData>(notificationClickedData)

            pushNotificationClickHandler.handleNotificationClick(event)

            verifySuspend {
                mockActionFactory.create(
                    NotificationOpenedActionModel(
                        null,
                        TRACKING_INFO
                    )
                )
            }
            verifySuspend { mockActionHandler.handleActions(listOf(mockReportingAction), null) }
        }

    @Test
    fun handleNotificationClick_shouldHandleException_whenClickEventIsNotDeserializable() =
        runTest {
            val event = "notParseableEvent"

            pushNotificationClickHandler.handleNotificationClick(event)

            verifySuspend(VerifyMode.exactly(0)) { mockActionFactory.create(any()) }
            verifySuspend(VerifyMode.exactly(0)) { mockActionHandler.handleActions(any(), any()) }
        }

    @Test
    fun handleNotificationClick_shouldHandleException_whenActionFactoryThrows() =
        runTest {
            val actionModel =
                PresentableOpenExternalUrlActionModel(
                    "actionId",
                    REPORTING,
                    "title",
                    "https://www.google.com"
                )
            val notificationClickedData =
                createTestJsNotificationClickedData(
                    "actionId",
                    actionModels = listOf(actionModel)
                )
            val event =
                JsonUtil.json.encodeToString<JsNotificationClickedData>(notificationClickedData)
            everySuspend { mockActionFactory.create(any()) } throws RuntimeException()

            pushNotificationClickHandler.handleNotificationClick(event)

            verifySuspend { mockActionFactory.create(actionModel) }
            verifyNoMoreCalls(mockAction)
        }

    private fun createTestJsNotificationClickedData(
        actionId: String,
        actionModels: List<PresentableActionModel>? = null,
        defaultTapActionModel: BasicActionModel? = null
    ) =
        JsNotificationClickedData(
            actionId = actionId,
            jsPushMessage = JsPushMessage(
                trackingInfo = TRACKING_INFO,
                platformData = JsPlatformData,
                badgeCount = null,
                actionableData = ActionableData(
                    actions = actionModels,
                    defaultTapAction = defaultTapActionModel
                ),
                displayableData = DisplayableData(
                    title = "title",
                    body = "body",
                    iconUrlString = null,
                    imageUrlString = null
                )
            )
        )
}
