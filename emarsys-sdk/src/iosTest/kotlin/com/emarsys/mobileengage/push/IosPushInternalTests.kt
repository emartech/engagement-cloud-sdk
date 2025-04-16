package com.emarsys.mobileengage.push

import com.emarsys.SdkConstants.PUSH_RECEIVED_EVENT_NAME
import com.emarsys.api.push.Ems
import com.emarsys.api.push.Notification
import com.emarsys.api.push.PushCall.ClearPushToken
import com.emarsys.api.push.PushCall.HandleSilentMessageWithUserInfo
import com.emarsys.api.push.PushCall.RegisterPushToken
import com.emarsys.api.push.PushConstants.PUSH_TOKEN_KEY
import com.emarsys.api.push.PushContext
import com.emarsys.api.push.PushContextApi
import com.emarsys.api.push.PushUserInfo
import com.emarsys.context.SdkContextApi
import com.emarsys.core.actions.ActionHandlerApi
import com.emarsys.core.actions.badge.BadgeCountHandlerApi
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.log.Logger
import com.emarsys.core.providers.InstantProvider
import com.emarsys.core.providers.UuidProviderApi
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.core.url.ExternalUrlOpenerApi
import com.emarsys.mobileengage.action.PushActionFactoryApi
import com.emarsys.mobileengage.action.actions.Action
import com.emarsys.mobileengage.action.actions.OpenExternalUrlAction
import com.emarsys.mobileengage.action.actions.ReportingAction
import com.emarsys.mobileengage.action.models.BadgeCount
import com.emarsys.mobileengage.action.models.BadgeCountMethod.SET
import com.emarsys.mobileengage.action.models.BasicOpenExternalUrlActionModel
import com.emarsys.mobileengage.action.models.BasicPushButtonClickedActionModel
import com.emarsys.mobileengage.action.models.NotificationOpenedActionModel
import com.emarsys.mobileengage.action.models.PresentableAppEventActionModel
import com.emarsys.mobileengage.action.models.PresentableOpenExternalUrlActionModel
import com.emarsys.networking.clients.event.model.SdkEvent
import com.emarsys.util.JsonUtil
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonPrimitive
import platform.UserNotifications.UNNotificationDefaultActionIdentifier
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class IosPushInternalTests {
    private companion object {
        const val UUID = "testUUID"
        const val PUSH_TOKEN = "testPushToken"
        const val VERSION = "APNS_V2"
        const val TRACKING_INFO = """{"trackingInfo":"testTrackingInfo"}"""
        const val REPORTING = """{"id":"testId"}"""
        const val REPORTING2 = """{"id":"testId2"}"""
        val REGISTER_PUSH_TOKEN = RegisterPushToken(PUSH_TOKEN)
        val CLEAR_PUSH_TOKEN = ClearPushToken()
        val HANDLE_SILENT_MESSAGE_WITH_USER_INFO = HandleSilentMessageWithUserInfo(
            PushUserInfo(
                ems = Ems(
                    version = "testVersion",
                    trackingInfo = "testTrackingInfo"
                ),
                notification = Notification(
                    silent = true,
                    actions = emptyList(),
                    badgeCount = BadgeCount(SET, 42)
                )
            )
        )

        val pushCalls = mutableListOf(
            REGISTER_PUSH_TOKEN,
            CLEAR_PUSH_TOKEN,
            HANDLE_SILENT_MESSAGE_WITH_USER_INFO

        )
    }

    private lateinit var iosPushInternal: IosPushInternal

    private lateinit var mockStorage: StringStorageApi
    private lateinit var pushContext: PushContextApi
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var mockActionFactory: PushActionFactoryApi
    private lateinit var mockActionHandler: ActionHandlerApi
    private lateinit var mockBadgeCountHandler: BadgeCountHandlerApi
    private lateinit var json: Json
    private lateinit var sdkDispatcher: CoroutineDispatcher
    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi
    private lateinit var mockSdkLogger: Logger
    private lateinit var mockTimestampProvider: InstantProvider
    private lateinit var mockUuidProvider: UuidProviderApi

    @BeforeTest
    fun setup() = runTest {
        Dispatchers.setMain(StandardTestDispatcher())

        mockStorage = mock()
        mockSdkContext = mock()
        mockActionFactory = mock()
        mockActionHandler = mock()
        mockBadgeCountHandler = mock()
        json = JsonUtil.json
        mockTimestampProvider = mock()
        mockUuidProvider = mock()
        sdkDispatcher = StandardTestDispatcher()
        mockSdkEventDistributor = mock(MockMode.autofill)
        mockSdkLogger = mock(MockMode.autofill)
        everySuspend { mockActionHandler.handleActions(any(), any()) } returns Unit
        everySuspend { mockTimestampProvider.provide() } returns Instant.DISTANT_PAST
        everySuspend { mockUuidProvider.provide() } returns UUID

        pushContext = PushContext(pushCalls)
        iosPushInternal = IosPushInternal(
            mockStorage,
            pushContext,
            mockSdkContext,
            mockActionFactory,
            mockActionHandler,
            mockBadgeCountHandler,
            json,
            sdkDispatcher,
            mockSdkLogger,
            mockSdkEventDistributor,
            mockTimestampProvider,
            mockUuidProvider
        )
    }

    @Test
    fun `didReceiveNotificationResponse should handle defaultAction`() = runTest {
        val actionModel =
            BasicOpenExternalUrlActionModel(REPORTING, url = "https://www.emarsys.com")
        val notificationOpenedActionModel = NotificationOpenedActionModel(REPORTING, TRACKING_INFO)
        val mockAction: Action<Unit> = mock(MockMode.autoUnit)
        val mockReportingAction: Action<Unit> = mock(MockMode.autoUnit)

        everySuspend { mockActionFactory.create(actionModel) } returns mockAction
        everySuspend { mockActionFactory.create(notificationOpenedActionModel) } returns mockReportingAction

        val actionIdentifier = UNNotificationDefaultActionIdentifier
        val userInfo = createUserInfoMap(
            defaultAction = mapOf(
                "type" to "OpenExternalUrl",
                "reporting" to REPORTING,
                "url" to "https://www.emarsys.com"
            ),
            actions = null
        )

        iosPushInternal.didReceiveNotificationResponse(actionIdentifier, userInfo) {}

        advanceUntilIdle()

        verifySuspend {
            mockActionFactory.create(actionModel)
            mockActionHandler.handleActions(listOf(mockReportingAction), mockAction)
        }
    }

    @Test
    fun `didReceiveNotificationResponse should handle action`() = runTest {
        val mockUrlOpener: ExternalUrlOpenerApi = mock(MockMode.autoUnit)
        val actionModel = PresentableOpenExternalUrlActionModel(
            "testId",
            reporting = REPORTING,
            title = "testTitle",
            url = "https://www.emarsys.com"
        )
        val action = OpenExternalUrlAction(actionModel, mockUrlOpener)

        everySuspend { mockActionFactory.create(any()) } returns action

        val actionIdentifier = "testId"
        val defaultActionMap = mapOf(
            "type" to "OpenExternalUrl",
            "id" to "testId2",
            "title" to "testTitle2",
            "reporting" to REPORTING2,
            "url" to "https://www.sap.com"
        )
        val actions = listOf(
            mapOf(
                "type" to "OpenExternalUrl",
                "id" to "testId",
                "title" to "testTitle",
                "reporting" to REPORTING,
                "url" to "https://www.emarsys.com"
            )
        )
        val userInfo = createUserInfoMap(
            defaultActionMap,
            actions
        )

        iosPushInternal.didReceiveNotificationResponse(actionIdentifier, userInfo) {}

        advanceUntilIdle()

        verifySuspend {
            mockActionFactory.create(actionModel)
            mockActionHandler.handleActions(any(), action)
        }
    }

    //TODO: revisit after Action modifications
//    @Test
//    fun `didReceiveNotificationResponse should handle pushToInAppAction`() = runTest {
//        val campaignId = "campaignId"
//        val mockPushToInAppHandler: PushToInAppHandlerApi = mock()
//
//        val actionModel = InternalPushToInappActionModel(campaignId, "https://www.emarsys.com")
//        val action = PushToInappAction(actionModel, mockPushToInAppHandler)
//
//        everySuspend {
//            mockActionFactory.create(actionModel)
//        } returns action
//        everySuspend {
//            mockPushToInAppHandler.handle(any())
//        } returns Unit
//
//        val actionIdentifier = UNNotificationDefaultActionIdentifier
//        val userInfo = mapOf(
//            "ems" to mapOf(
//                "version" to VERSION,
//                "trackingInfo" to TRACKING_INFO
//            ),
//            "notification" to mapOf(
//                "actions" to listOf(
//                    mapOf(
//                        "name" to "testName",
//                        "payload" to mapOf(
//                            "campaignId" to campaignId,
//                            "url" to "https://www.emarsys.com"
//                        )
//                    )
//                )
//            )
//        )
//
//        iosPushInternal.didReceiveNotificationResponse(actionIdentifier, userInfo) {}
//
//        advanceUntilIdle()
//
//        verifySuspend {
//            mockActionFactory.create(actionModel)
//            mockActionHandler.handleActions(any(), action)
//        }
//    }

    @Test
    fun `didReceiveNotificationResponse should handle badgeCount if present`() = runTest {
        val actionIdentifier = UNNotificationDefaultActionIdentifier
        everySuspend { mockActionFactory.create(NotificationOpenedActionModel(null, TRACKING_INFO)) } returns mock()
        val userInfo = createUserInfoMap(
            null,
            null,
            badgeCount = mapOf(
                "method" to "set",
                "value" to 42
            )
        )
        everySuspend { mockBadgeCountHandler.handle(any()) } returns Unit
        val expectedBadgeCount = BadgeCount(SET, 42)

        iosPushInternal.didReceiveNotificationResponse(actionIdentifier, userInfo) {}

        advanceUntilIdle()

        verifySuspend {
            mockBadgeCountHandler.handle(expectedBadgeCount)
        }
    }

    @Test
    fun `didReceiveNotificationResponse should report buttonClick`() = runTest {
        val mockUrlOpener: ExternalUrlOpenerApi = mock()
        val actionModel = PresentableOpenExternalUrlActionModel(
            id = "testId",
            reporting = REPORTING,
            title = "testTitle",
            url = "https://www.emarsys.com"
        )
        val action = OpenExternalUrlAction(actionModel, mockUrlOpener)

        everySuspend {
            mockActionFactory.create(actionModel)
        } returns action
        everySuspend {
            mockUrlOpener.open(any())
        } returns Unit

        val actionIdentifier = "testId"
        val userInfo = createUserInfoMap(
            null, listOf(
                mapOf(
                    "type" to "OpenExternalUrl",
                    "id" to "testId",
                    "title" to "testTitle",
                    "reporting" to REPORTING,
                    "url" to "https://www.emarsys.com"
                )
            )
        )
        val buttonClickedActionModel = BasicPushButtonClickedActionModel(
            actionModel.reporting,
            TRACKING_INFO
        )
        val reportingAction = ReportingAction(buttonClickedActionModel, mock(MockMode.autoUnit))

        everySuspend { mockActionFactory.create(buttonClickedActionModel) } returns reportingAction

        iosPushInternal.didReceiveNotificationResponse(actionIdentifier, userInfo) {}

        advanceUntilIdle()

        verifySuspend {
            mockActionFactory.create(actionModel)
            mockActionHandler.handleActions(listOf(reportingAction), action)
        }
    }

    @Test
    fun `didReceiveNotificationResponse should report defaultAction`() =
        runTest {
            val mockUrlOpener: ExternalUrlOpenerApi = mock()
            val actionModel =
                BasicOpenExternalUrlActionModel(REPORTING, url = "https://www.emarsys.com")
            val action = OpenExternalUrlAction(actionModel, mockUrlOpener)

            everySuspend { mockActionFactory.create(actionModel) } returns action
            everySuspend { mockUrlOpener.open(any()) } returns Unit

            val actionIdentifier = UNNotificationDefaultActionIdentifier
            val userInfo = createUserInfoMap(
                defaultAction = mapOf(
                    "type" to "OpenExternalUrl",
                    "reporting" to REPORTING,
                    "url" to "https://www.emarsys.com"
                ),
                actions = listOf(
                    mapOf(
                        "type" to "OpenExternalUrl",
                        "id" to "testId",
                        "title" to "testTitle",
                        "reporting" to REPORTING,
                        "url" to "https://www.emarsys.com"
                    )
                )
            )

            val notificationOpenedActionModel =
                NotificationOpenedActionModel(REPORTING, TRACKING_INFO)

            val reportingAction = ReportingAction(notificationOpenedActionModel, mock())

            everySuspend { mockActionFactory.create(notificationOpenedActionModel) } returns reportingAction

            iosPushInternal.didReceiveNotificationResponse(actionIdentifier, userInfo) {}

            advanceUntilIdle()

            verifySuspend {
                mockActionFactory.create(actionModel)
                mockActionHandler.handleActions(listOf(reportingAction), action)
            }
        }

    @Test
    fun `didReceiveNotificationResponse should execute mandatory actions event if there was no action triggered`() =
        runTest {
            val actionIdentifier = UNNotificationDefaultActionIdentifier
            val userInfo = createUserInfoMap(
                defaultAction = null,
                actions = null
            )

            val notificationOpenedActionModel = NotificationOpenedActionModel(null, TRACKING_INFO)

            val reportingAction = ReportingAction(notificationOpenedActionModel, mock())

            everySuspend { mockActionFactory.create(notificationOpenedActionModel) } returns reportingAction

            iosPushInternal.didReceiveNotificationResponse(actionIdentifier, userInfo) {}

            advanceUntilIdle()

            verifySuspend {
                mockActionHandler.handleActions(listOf(reportingAction), null)
            }
        }

    @Test
    fun `handleMessageWithUserInfo should execute actions`() = runTest {
        val openExternalUrlActionModel = PresentableOpenExternalUrlActionModel(
            id = "testId",
            reporting = REPORTING,
            title = "OpenExternalUrlAction",
            url = "https://www.emarsys.com"
        )
        val appEventActionModel = PresentableAppEventActionModel(
            "testId",
            REPORTING2,
            "testTitle",
            "name",
            mapOf("key" to "value")
        )
        val pushUserInfo =
            PushUserInfo(
                ems = Ems(
                    version = VERSION,
                    trackingInfo = TRACKING_INFO
                ),
                notification = Notification(
                    actions = listOf(
                        appEventActionModel,
                        openExternalUrlActionModel
                    )
                ),
            )

        val mockOpenExternalUrlAction: Action<*> = mock(MockMode.autoUnit)
        val mockAppEventAction: Action<*> = mock(MockMode.autoUnit)
        everySuspend { mockSdkEventDistributor.registerEvent(any()) } returns mock(MockMode.autofill)

        everySuspend { mockActionFactory.create(openExternalUrlActionModel) } returns mockOpenExternalUrlAction
        everySuspend { mockActionFactory.create(appEventActionModel) } returns mockAppEventAction

        iosPushInternal.handleSilentMessageWithUserInfo(pushUserInfo)

        verifySuspend { mockActionFactory.create(openExternalUrlActionModel) }
        verifySuspend { mockActionFactory.create(appEventActionModel) }
        verifySuspend { mockOpenExternalUrlAction.invoke() }
        verifySuspend { mockAppEventAction.invoke() }
    }

    @Test
    fun `handleMessageWithUserInfo should emit event with campaignId`() = runTest {
        val openExternalUrlActionModel =
            PresentableOpenExternalUrlActionModel(
                "testId",
                REPORTING,
                "testTitle",
                "https://www.emarsys.com"
            )
        val mockOpenExternalUrlAction = mock<Action<*>>()
        everySuspend { mockOpenExternalUrlAction.invoke() } returns Unit

        val userInfo = PushUserInfo(
            ems = Ems(
                version = VERSION,
                trackingInfo = TRACKING_INFO
            ),
            notification = Notification(
                actions = listOf(
                    openExternalUrlActionModel
                )
            ),
        )
        everySuspend { mockActionFactory.create(openExternalUrlActionModel) } returns mockOpenExternalUrlAction
        everySuspend { mockSdkEventDistributor.registerEvent(any()) } returns mock(MockMode.autofill)

        iosPushInternal.handleSilentMessageWithUserInfo(userInfo)

        verifySuspend { mockOpenExternalUrlAction.invoke() }
        verifySuspend {
            mockSdkEventDistributor.registerEvent(
                SdkEvent.External.Api.SilentPush(
                    id = UUID,
                    name = PUSH_RECEIVED_EVENT_NAME,
                    timestamp = Instant.DISTANT_PAST
                )
            )
        }
    }

    @Test
    fun `testActivate should handle pushCalls`() = runTest {
        val eventContainer = Capture.container<SdkEvent>()
        everySuspend { mockSdkEventDistributor.registerEvent(capture(eventContainer)) } returns mock(
            MockMode.autofill
        )

        iosPushInternal.activate()

        advanceUntilIdle()

        val emittedValues = eventContainer.values
        emittedValues.first { it is SdkEvent.Internal.Sdk.RegisterPushToken }.apply {
            this.attributes?.get(PUSH_TOKEN_KEY)?.jsonPrimitive?.content shouldBe PUSH_TOKEN
        }
        emittedValues.firstOrNull { it is SdkEvent.Internal.Sdk.ClearPushToken } shouldNotBe null
        emittedValues.firstOrNull { it is SdkEvent.External.Api.SilentPush } shouldNotBe null
    }

    private fun createUserInfoMap(
        defaultAction: Map<String, Any>? = null,
        actions: List<Map<String, Any>>? = null,
        badgeCount: Map<String, Any>? = null
    ): Map<String, Any> {
        return buildMap {
            put(
                "ems", mapOf(
                    "version" to VERSION,
                    "trackingInfo" to TRACKING_INFO
                )
            )
            put("notification", buildMap {
                defaultAction?.let { put("defaultAction", it) }
                actions?.let { put("actions", it) }
                badgeCount?.let { put("badgeCount", it) }
            })
        }
    }
}