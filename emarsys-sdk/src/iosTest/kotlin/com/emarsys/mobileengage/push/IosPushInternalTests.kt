package com.emarsys.mobileengage.push

import com.emarsys.api.AppEvent
import com.emarsys.api.generic.ApiContext
import com.emarsys.api.push.PushCall
import com.emarsys.context.SdkContextApi
import com.emarsys.core.pushtoinapp.PushToInAppHandlerApi
import com.emarsys.core.storage.TypedStorageApi
import com.emarsys.core.url.ExternalUrlOpenerApi
import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.actions.OpenExternalUrlAction
import com.emarsys.mobileengage.action.actions.PushToInappAction
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.action.models.InternalPushToInappActionModel
import com.emarsys.mobileengage.action.models.PresentableOpenExternalUrlActionModel
import com.emarsys.networking.clients.push.PushClientApi
import com.emarsys.util.JsonUtil
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import platform.UserNotifications.UNNotificationDefaultActionIdentifier
import kotlin.test.BeforeTest
import kotlin.test.Test

@ExperimentalCoroutinesApi
class IosPushInternalTests {

    private lateinit var iosPushInternal: IosPushInternal

    private lateinit var mockPushClient: PushClientApi
    private lateinit var mockStorage: TypedStorageApi<String?>
    private lateinit var mockPushContext: ApiContext<PushCall>
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var notificationEvents: MutableSharedFlow<AppEvent>
    private lateinit var mockActionFactory: ActionFactoryApi<ActionModel>
    private lateinit var json: Json
    private lateinit var sdkDispatcher: CoroutineDispatcher

    @BeforeTest
    fun setup() = runTest {
        val dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)

        mockPushClient = mock()
        mockStorage = mock()
        mockPushContext = mock()
        mockSdkContext = mock()
        notificationEvents = MutableSharedFlow()
        mockActionFactory = mock()
        json = JsonUtil.json
        sdkDispatcher = dispatcher

        iosPushInternal = IosPushInternal(
            mockPushClient,
            mockStorage,
            mockPushContext,
            mockSdkContext,
            notificationEvents,
            mockActionFactory,
            json,
            sdkDispatcher
        )
    }

    @Test
    fun `didReceiveNotificationResponse should execute defaultAction`() = runTest {
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
        } returns true

        val actionIdentifier = UNNotificationDefaultActionIdentifier
        val userInfo = mapOf(
            "ems" to mapOf(
                "default_action" to mapOf(
                    "type" to "OpenExternalUrl",
                    "title" to "testTitle",
                    "id" to "testId",
                    "url" to "https://www.emarsys.com"
                )
            )
        )

        iosPushInternal.didReceiveNotificationResponse(actionIdentifier, userInfo) {}

        advanceUntilIdle()

        verifySuspend {
            mockActionFactory.create(actionModel)
            mockUrlOpener.open("https://www.emarsys.com")
        }
    }

    @Test
    fun `didReceiveNotificationResponse should execute action`() = runTest {
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
        } returns true

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
            mockUrlOpener.open("https://www.emarsys.com")
        }
    }

    @Test
    fun `didReceiveNotificationResponse should execute pushToInAppAction`() = runTest {
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
            mockPushToInAppHandler.handle(actionModel)
        }
    }
}