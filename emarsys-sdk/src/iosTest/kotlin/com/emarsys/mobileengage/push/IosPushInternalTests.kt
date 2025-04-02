package com.emarsys.mobileengage.push

import com.emarsys.SdkConstants.PUSH_RECEIVED_EVENT_NAME
import com.emarsys.api.push.BasicPushUserInfo
import com.emarsys.api.push.BasicPushUserInfoEms
import com.emarsys.api.push.PushCall.ClearPushToken
import com.emarsys.api.push.PushCall.HandleMessageWithUserInfo
import com.emarsys.api.push.PushCall.RegisterPushToken
import com.emarsys.api.push.PushContext
import com.emarsys.api.push.PushContextApi
import com.emarsys.context.SdkContextApi
import com.emarsys.core.actions.ActionHandlerApi
import com.emarsys.core.actions.badge.BadgeCountHandlerApi
import com.emarsys.core.actions.pushtoinapp.PushToInAppHandlerApi
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.log.Logger
import com.emarsys.core.providers.InstantProvider
import com.emarsys.core.providers.UuidProviderApi
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.core.url.ExternalUrlOpenerApi
import com.emarsys.mobileengage.action.PushActionFactoryApi
import com.emarsys.mobileengage.action.actions.Action
import com.emarsys.mobileengage.action.actions.OpenExternalUrlAction
import com.emarsys.mobileengage.action.actions.PushToInappAction
import com.emarsys.mobileengage.action.actions.ReportingAction
import com.emarsys.mobileengage.action.models.BadgeCount
import com.emarsys.mobileengage.action.models.BadgeCountMethod.SET
import com.emarsys.mobileengage.action.models.BasicAppEventActionModel
import com.emarsys.mobileengage.action.models.BasicOpenExternalUrlActionModel
import com.emarsys.mobileengage.action.models.BasicPushButtonClickedActionModel
import com.emarsys.mobileengage.action.models.InternalPushToInappActionModel
import com.emarsys.mobileengage.action.models.NotificationOpenedActionModel
import com.emarsys.mobileengage.action.models.PresentableOpenExternalUrlActionModel
import com.emarsys.networking.clients.event.model.SdkEvent
import com.emarsys.networking.clients.push.PushClientApi
import com.emarsys.util.JsonUtil
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import platform.UserNotifications.UNNotificationDefaultActionIdentifier
import kotlin.test.BeforeTest
import kotlin.test.Test

@ExperimentalCoroutinesApi
internal class IosPushInternalTests {
    private companion object {
        const val SID = "testSid"
        const val CAMPAIGN_ID = "campaignId"
        const val UUID = "testUUID"
        const val PUSH_TOKEN = "testPushToken"
        val REGISTER_PUSH_TOKEN = RegisterPushToken(PUSH_TOKEN)
        val CLEAR_PUSH_TOKEN = ClearPushToken()
        val HANDLE_MESSAGE_WITH_USER_INFO = HandleMessageWithUserInfo(
            BasicPushUserInfo(
                ems = BasicPushUserInfoEms(
                    multichannelId = CAMPAIGN_ID,
                    sid = SID,
                    actions = emptyList()
                )
            )
        )

        val pushCalls = mutableListOf(
            REGISTER_PUSH_TOKEN,
            CLEAR_PUSH_TOKEN,
            HANDLE_MESSAGE_WITH_USER_INFO

        )
    }

    private lateinit var iosPushInternal: IosPushInternal

    private lateinit var mockPushClient: PushClientApi
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
        val dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)

        mockPushClient = mock()
        mockStorage = mock()
        mockSdkContext = mock()
        mockActionFactory = mock()
        mockActionHandler = mock()
        mockBadgeCountHandler = mock()
        json = JsonUtil.json
        mockTimestampProvider = mock()
        mockUuidProvider = mock()
        sdkDispatcher = dispatcher
        mockSdkEventDistributor = mock()
        mockSdkLogger = mock(MockMode.autofill)
        everySuspend { mockActionHandler.handleActions(any(), any()) } returns Unit
        everySuspend { mockTimestampProvider.provide() } returns Instant.DISTANT_PAST
        everySuspend { mockUuidProvider.provide() } returns UUID

        pushContext = PushContext(pushCalls)
        iosPushInternal = IosPushInternal(
            mockPushClient,
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
        val mockUrlOpener: ExternalUrlOpenerApi = mock()
        val actionModel = BasicOpenExternalUrlActionModel(url = "https://www.emarsys.com")
        val action = OpenExternalUrlAction(actionModel, mockUrlOpener)

        everySuspend {
            mockActionFactory.create(any())
        } returns action
        everySuspend {
            mockUrlOpener.open(any())
        } returns Unit

        val actionIdentifier = UNNotificationDefaultActionIdentifier
        val userInfo = mapOf(
            "ems" to mapOf(
                "default_action" to mapOf(
                    "type" to "OpenExternalUrl",
                    "url" to "https://www.emarsys.com"
                )
            )
        )

        iosPushInternal.didReceiveNotificationResponse(actionIdentifier, userInfo) {}

        advanceUntilIdle()

        verifySuspend {
            mockActionFactory.create(actionModel)
            mockActionHandler.handleActions(any(), action)
        }
    }

    @Test
    fun `didReceiveNotificationResponse should handle action`() = runTest {
        val mockUrlOpener: ExternalUrlOpenerApi = mock()
        val actionModel = PresentableOpenExternalUrlActionModel(
            id = "testId",
            title = "testTitle",
            url = "https://www.emarsys.com"
        )
        val action = OpenExternalUrlAction(actionModel, mockUrlOpener)

        everySuspend {
            mockActionFactory.create(any())
        } returns action
        everySuspend {
            mockUrlOpener.open(any())
        } returns Unit

        val actionIdentifier = "testId"
        val userInfo = mapOf(
            "ems" to mapOf(
                "default_action" to mapOf(
                    "type" to "OpenExternalUrl",
                    "title" to "testTitle2",
                    "id" to "testId2",
                    "url" to "https://www.sap.com"
                ),
                "actions" to listOf(
                    mapOf(
                        "type" to "OpenExternalUrl",
                        "title" to "testTitle",
                        "id" to "testId",
                        "url" to "https://www.emarsys.com"
                    )
                )
            )
        )

        iosPushInternal.didReceiveNotificationResponse(actionIdentifier, userInfo) {}

        advanceUntilIdle()

        verifySuspend {
            mockActionFactory.create(actionModel)
            mockActionHandler.handleActions(any(), action)
        }
    }

    @Test
    fun `didReceiveNotificationResponse should handle pushToInAppAction`() = runTest {
        val campaignId = "campaignId"
        val mockPushToInAppHandler: PushToInAppHandlerApi = mock()

        val actionModel = InternalPushToInappActionModel(campaignId, "https://www.emarsys.com")
        val action = PushToInappAction(actionModel, mockPushToInAppHandler)

        everySuspend {
            mockActionFactory.create(actionModel)
        } returns action
        everySuspend {
            mockPushToInAppHandler.handle(any())
        } returns Unit

        val actionIdentifier = UNNotificationDefaultActionIdentifier
        val userInfo = mapOf(
            "ems" to mapOf(
                "inapp" to mapOf(
                    "campaign_id" to campaignId,
                    "url" to "https://www.emarsys.com"
                )
            )
        )

        iosPushInternal.didReceiveNotificationResponse(actionIdentifier, userInfo) {}

        advanceUntilIdle()

        verifySuspend {
            mockActionFactory.create(actionModel)
            mockActionHandler.handleActions(any(), action)
        }
    }

    @Test
    fun `didReceiveNotificationResponse should handle badgeCount if present`() = runTest {
        val actionIdentifier = UNNotificationDefaultActionIdentifier
        val userInfo = mapOf(
            "ems" to mapOf(
                "badgeCount" to mapOf("method" to "SET", "value" to "42")
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
    fun `didReceiveNotificationResponse should report buttonClick_withOldApnsPayload`() = runTest {
        val mockUrlOpener: ExternalUrlOpenerApi = mock()
        val actionModel = PresentableOpenExternalUrlActionModel(
            id = "testId",
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
        val userInfo = mapOf(
            "u" to mapOf(
                "sid" to SID,
            ),
            "ems" to mapOf(
                "actions" to listOf(
                    mapOf(
                        "type" to "OpenExternalUrl",
                        "title" to "testTitle",
                        "id" to "testId",
                        "url" to "https://www.emarsys.com"
                    )
                )
            )
        )

        val buttonClickedActionModel = BasicPushButtonClickedActionModel(
            actionModel.id,
            SID
        )
        val reportingAction = ReportingAction(buttonClickedActionModel, mock())

        everySuspend { mockActionFactory.create(buttonClickedActionModel) } returns reportingAction


        iosPushInternal.didReceiveNotificationResponse(actionIdentifier, userInfo) {}

        advanceUntilIdle()

        verifySuspend {
            mockActionFactory.create(actionModel)
            mockActionHandler.handleActions(listOf(reportingAction), action)
        }
    }

    @Test
    fun `didReceiveNotificationResponse should report buttonClick_withNewApnsPayload`() = runTest {
        val mockUrlOpener: ExternalUrlOpenerApi = mock()
        val actionModel = PresentableOpenExternalUrlActionModel(
            id = "testId",
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
        val userInfo = mapOf(
            "ems" to mapOf(
                "sid" to SID,
                "actions" to listOf(
                    mapOf(
                        "type" to "OpenExternalUrl",
                        "title" to "testTitle",
                        "id" to "testId",
                        "url" to "https://www.emarsys.com"
                    )
                )
            )
        )

        val buttonClickedActionModel = BasicPushButtonClickedActionModel(
            actionModel.id,
            SID
        )
        val reportingAction = ReportingAction(buttonClickedActionModel, mock())

        everySuspend { mockActionFactory.create(buttonClickedActionModel) } returns reportingAction


        iosPushInternal.didReceiveNotificationResponse(actionIdentifier, userInfo) {}

        advanceUntilIdle()

        verifySuspend {
            mockActionFactory.create(actionModel)
            mockActionHandler.handleActions(listOf(reportingAction), action)
        }
    }

    @Test
    fun `didReceiveNotificationResponse should report defaultAction_withOldApnsPayload`() =
        runTest {
            val mockUrlOpener: ExternalUrlOpenerApi = mock()
            val actionModel = BasicOpenExternalUrlActionModel(url = "https://www.emarsys.com")
            val action = OpenExternalUrlAction(actionModel, mockUrlOpener)

            everySuspend { mockActionFactory.create(actionModel) } returns action
            everySuspend { mockUrlOpener.open(any()) } returns Unit

            val actionIdentifier = UNNotificationDefaultActionIdentifier
            val userInfo = mapOf(
                "u" to mapOf(
                    "sid" to SID,
                ),
                "ems" to mapOf(
                    "default_action" to mapOf(
                        "type" to "OpenExternalUrl",
                        "url" to "https://www.emarsys.com"
                    ),
                    "actions" to listOf(
                        mapOf(
                            "type" to "OpenExternalUrl",
                            "title" to "testTitle",
                            "id" to "testId",
                            "url" to "https://www.emarsys.com"
                        )
                    )
                )
            )

            val notificationOpenedActionModel = NotificationOpenedActionModel(SID)

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
    fun `didReceiveNotificationResponse should report defaultAction_withNewApnsPayload`() =
        runTest {
            val mockUrlOpener: ExternalUrlOpenerApi = mock()
            val actionModel = BasicOpenExternalUrlActionModel(url = "https://www.emarsys.com")
            val action = OpenExternalUrlAction(actionModel, mockUrlOpener)

            everySuspend { mockActionFactory.create(actionModel) } returns action
            everySuspend { mockUrlOpener.open(any()) } returns Unit

            val actionIdentifier = UNNotificationDefaultActionIdentifier

            val userInfo = mapOf(
                "ems" to mapOf(
                    "sid" to SID,
                    "defaultAction" to mapOf(
                        "type" to "OpenExternalUrl",
                        "url" to "https://www.emarsys.com"
                    ),
                    "actions" to listOf(
                        mapOf(
                            "type" to "OpenExternalUrl",
                            "title" to "testTitle",
                            "id" to "testId",
                            "url" to "https://www.emarsys.com"
                        )
                    )
                )
            )

            val notificationOpenedActionModel = NotificationOpenedActionModel(SID)

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
    fun `handleMessageWithUserInfo should execute actions`() = runTest {
        val userInfo = BasicPushUserInfo(
            ems = BasicPushUserInfoEms(
                multichannelId = CAMPAIGN_ID,
                sid = SID,
                actions = listOf(
                    BasicOpenExternalUrlActionModel(url = "https://www.emarsys.com"),
                    BasicAppEventActionModel("name", mapOf("key" to "value"))
                )
            )
        )

        val openExternalActionModel =
            BasicOpenExternalUrlActionModel(url = "https://www.emarsys.com")
        val appEventActionModel = BasicAppEventActionModel("name", mapOf("key" to "value"))
        val mockOpenExternalUrlAction: Action<*> = mock()
        val mockAppEventAction: Action<*> = mock()
        everySuspend { mockSdkEventDistributor.registerAndStoreEvent(any()) } returns Unit

        everySuspend { mockOpenExternalUrlAction.invoke() } returns Unit
        everySuspend { mockAppEventAction.invoke() } returns Unit
        everySuspend { mockActionFactory.create(openExternalActionModel) } returns mockOpenExternalUrlAction
        everySuspend { mockActionFactory.create(appEventActionModel) } returns mockAppEventAction

        iosPushInternal.handleSilentMessageWithUserInfo(userInfo)

        verifySuspend { mockActionFactory.create(openExternalActionModel) }
        verifySuspend { mockActionFactory.create(appEventActionModel) }
        verifySuspend { mockOpenExternalUrlAction.invoke() }
        verifySuspend { mockAppEventAction.invoke() }
    }

    @Test
    fun `handleMessageWithUserInfo should emit event with campaignId`() = runTest {
        val openExternalUrlActionModel =
            BasicOpenExternalUrlActionModel(url = "https://www.emarsys.com")
        val mockOpenExternalUrlAction = mock<Action<*>>()
        everySuspend { mockOpenExternalUrlAction.invoke() } returns Unit

        val userInfo = BasicPushUserInfo(
            ems = BasicPushUserInfoEms(
                multichannelId = CAMPAIGN_ID,
                sid = SID,
                actions = listOf(
                    openExternalUrlActionModel
                )
            )
        )
        everySuspend { mockActionFactory.create(openExternalUrlActionModel) } returns mockOpenExternalUrlAction
        everySuspend { mockSdkEventDistributor.registerAndStoreEvent(any()) } returns Unit

        iosPushInternal.handleSilentMessageWithUserInfo(userInfo)

        verifySuspend { mockOpenExternalUrlAction.invoke() }
        verifySuspend {
            mockSdkEventDistributor.registerAndStoreEvent(
                SdkEvent.External.Api.SilentPush(
                    id = UUID,
                    name = PUSH_RECEIVED_EVENT_NAME,
                    attributes = buildJsonObject { put("campaignId", JsonPrimitive(CAMPAIGN_ID)) },
                    timestamp = Instant.DISTANT_PAST
                )
            )
        }
    }

    @Test
    fun `testActivate should handle pushCalls`() = runTest {
        everySuspend { mockPushClient.registerPushToken(any()) } returns Unit
        everySuspend { mockPushClient.clearPushToken() } returns Unit
        everySuspend { mockSdkEventDistributor.registerAndStoreEvent(any()) } returns Unit

        iosPushInternal.activate()

        advanceUntilIdle()

        verifySuspend { mockPushClient.registerPushToken(PUSH_TOKEN) }
        verifySuspend { mockPushClient.clearPushToken() }
        verifySuspend {
            mockSdkEventDistributor.registerAndStoreEvent(
                SdkEvent.External.Api.SilentPush(
                    id = UUID,
                    name = PUSH_RECEIVED_EVENT_NAME,
                    attributes = buildJsonObject { put("campaignId", JsonPrimitive(CAMPAIGN_ID)) },
                    timestamp = Instant.DISTANT_PAST
                )
            )
        }
    }
}