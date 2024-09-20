package com.emarsys.mobileengage.push

import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.actions.Action
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.action.models.BasicOpenExternalUrlActionModel
import com.emarsys.mobileengage.action.models.PresentableActionModel
import com.emarsys.mobileengage.action.models.PresentableOpenExternalUrlActionModel
import com.emarsys.mobileengage.push.model.JsNotificationClickedData
import com.emarsys.mobileengage.push.model.JsPlatformData
import com.emarsys.mobileengage.push.model.JsPushMessage
import com.emarsys.util.JsonUtil
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifyNoMoreCalls
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.encodeToString
import web.broadcast.BroadcastChannel
import kotlin.test.BeforeTest
import kotlin.test.Test

class PushNotificationClickHandlerTests {

    private lateinit var mockActionFactory: ActionFactoryApi<ActionModel>
    private lateinit var mockAction: Action<*>
    private lateinit var onNotificationClickedBroadcastChannel: BroadcastChannel
    private lateinit var pushNotificationClickHandler: PushNotificationClickHandler

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeTest
    fun setup() = runTest {
        mockActionFactory = mock<ActionFactoryApi<ActionModel>>()
        mockAction = createMockAction()
        everySuspend { mockActionFactory.create(any()) } returns mockAction

        onNotificationClickedBroadcastChannel =
            BroadcastChannel("emarsys-sdk-on-notification-clicked-channel")
        Dispatchers.setMain(StandardTestDispatcher())
        pushNotificationClickHandler =
            PushNotificationClickHandler(
                mockActionFactory,
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
    fun handleNotificationClick_shouldCallActionFactory_whenDefaultTapAction_isPresent() = runTest {
        val defaultTapActionModel = BasicOpenExternalUrlActionModel("https://www.google.com")
        val notificationClickedData =
            createTestJsNotificationClickedData("", defaultTapActionModel = defaultTapActionModel)
        val event =
            JsonUtil.json.encodeToString<JsNotificationClickedData>(notificationClickedData)

        pushNotificationClickHandler.handleNotificationClick(event)

        verifySuspend { mockActionFactory.create(defaultTapActionModel) }
        verifySuspend { mockAction.invoke() }
    }

    @Test
    fun handleNotificationClick_shouldCallActionFactory_whenActionWithId_isPresent() = runTest {
        val actionId = "testActionId"
        val actionModel =
            PresentableOpenExternalUrlActionModel(actionId, "title", "https://www.google.com")
        val notificationClickedData =
            createTestJsNotificationClickedData(actionId, actionModels = listOf(actionModel))
        val event =
            JsonUtil.json.encodeToString<JsNotificationClickedData>(notificationClickedData)

        pushNotificationClickHandler.handleNotificationClick(event)

        verifySuspend { mockActionFactory.create(actionModel) }
        verifySuspend { mockAction.invoke() }
    }

    @Test
    fun handleNotificationClick_shouldNotCallActionFactory_whenActionWithId_isNotPresent() =
        runTest {
            val actionModel =
                PresentableOpenExternalUrlActionModel("actionId", "title", "https://www.google.com")
            val notificationClickedData =
                createTestJsNotificationClickedData(
                    "differentClickedActionId",
                    actionModels = listOf(actionModel)
                )
            val event =
                JsonUtil.json.encodeToString<JsNotificationClickedData>(notificationClickedData)

            pushNotificationClickHandler.handleNotificationClick(event)

            verifySuspend(VerifyMode.exactly(0)) { mockActionFactory.create(actionModel) }
            verifySuspend(VerifyMode.exactly(0)) { mockAction.invoke() }
        }

    @Test
    fun handleNotificationClick_shouldNotCallActionFactory_whenDefaultTapAction_isNotPresent() =
        runTest {
            val actionModel =
                PresentableOpenExternalUrlActionModel("actionId", "title", "https://www.google.com")
            val notificationClickedData =
                createTestJsNotificationClickedData(
                    "",
                    defaultTapActionModel = null,
                    actionModels = listOf(actionModel)
                )
            val event =
                JsonUtil.json.encodeToString<JsNotificationClickedData>(notificationClickedData)

            pushNotificationClickHandler.handleNotificationClick(event)

            verifyNoMoreCalls(mockActionFactory)
            verifyNoMoreCalls(mockAction)
        }

    @Test
    fun handleNotificationClick_shouldHandleException_whenClickEventIsNotDeserializable() =
        runTest {
            val event = "notParseableEvent"

            pushNotificationClickHandler.handleNotificationClick(event)

            verifyNoMoreCalls(mockActionFactory)
        }

    @Test
    fun handleNotificationClick_shouldHandleException_whenActionFactoryThrows() =
        runTest {
            val actionModel =
                PresentableOpenExternalUrlActionModel("actionId", "title", "https://www.google.com")
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
        defaultTapActionModel: BasicOpenExternalUrlActionModel? = null
    ) =
        JsNotificationClickedData(
            actionId = actionId,
            jsPushMessage = JsPushMessage(
                messageId = "messageId",
                title = "title",
                body = "body",
                data = PushData(
                    sid = "sid",
                    campaignId = "campaignId",
                    platformData = JsPlatformData(
                        applicationCode = "applicationCode",
                    ),
                    actions = actionModels,
                    defaultTapAction = defaultTapActionModel
                )
            )
        )

    private fun createMockAction(): Action<*> =
        mock { everySuspend { invoke() } returns Unit }

}