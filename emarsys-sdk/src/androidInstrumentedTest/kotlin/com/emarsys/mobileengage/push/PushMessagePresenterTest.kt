package com.emarsys.mobileengage.push

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.emarsys.core.device.AndroidNotificationSettings
import com.emarsys.core.device.ChannelSettings
import com.emarsys.core.device.PlatformInfoCollector
import com.emarsys.core.resource.MetadataReader
import com.emarsys.mobileengage.action.models.BadgeCount
import com.emarsys.mobileengage.action.models.BadgeCountMethod
import com.emarsys.mobileengage.action.models.BasicAppEventActionModel
import com.emarsys.mobileengage.action.models.PresentableActionModel
import com.emarsys.mobileengage.action.models.PresentableAppEventActionModel
import com.emarsys.mobileengage.action.models.PresentableCustomEventActionModel
import com.emarsys.mobileengage.action.models.PresentableOpenExternalUrlActionModel
import com.emarsys.mobileengage.inapp.InAppDownloader
import com.emarsys.mobileengage.push.model.AndroidPlatformData
import com.emarsys.mobileengage.push.model.AndroidPushMessage
import com.emarsys.mobileengage.push.model.NotificationMethod
import com.emarsys.mobileengage.push.model.NotificationOperation.INIT
import com.emarsys.util.JsonUtil
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.CapturingSlot
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Before
import org.junit.Test

class PushMessagePresenterTest {
    private companion object {
        const val COLLAPSE_ID = "testCollapseId"
        const val ICON_ID = 10
        const val CHANNEL_ID = "testChannelId"
        const val MESSAGE_ID = "testMessageId"
        const val DEBUG_CHANNEL_ID = "ems_debug"
        const val TITLE = "testTitle"
        const val BODY = "testBody"
        const val SID = "testSid"
        const val CAMPAIGN_ID = "testCampaignId"
        val testCustomEventAction =
            PresentableCustomEventActionModel(
                "customEventId",
                "customEvent",
                "customAction",
                mapOf("key" to "value")
            )
        val testAppEventAction =
            PresentableAppEventActionModel(
                "appEventId",
                "appEvent",
                "appAction",
                mapOf("key2" to "value2")
            )
        val testOpenExternalUrlAction =
            PresentableOpenExternalUrlActionModel(
                "externalUrlId",
                "openExternalUrl",
                "https://example.com"
            )

        val testPresentableActions: List<PresentableActionModel> =
            listOf(testCustomEventAction, testAppEventAction, testOpenExternalUrlAction)

        val testDefaultTapAction = BasicAppEventActionModel("testName", null)
    }

    private lateinit var pushMessagePresenter: PushMessagePresenter
    private lateinit var mockContext: Context
    private lateinit var mockNotificationManager: NotificationManager
    private lateinit var json: Json
    private lateinit var mockMetadataReader: MetadataReader
    private lateinit var mockNotificationCompatStyler: NotificationCompatStyler
    private lateinit var mockPlatformInfoCollector: PlatformInfoCollector
    private lateinit var mockInAppDownloader: InAppDownloader
    private lateinit var notificationSlot: CapturingSlot<Notification>

    @Before
    fun setup() = runTest {
        mockContext = getInstrumentation().targetContext.applicationContext
        mockNotificationManager = mockk(relaxed = true)
        mockNotificationCompatStyler = mockk(relaxed = true)
        mockPlatformInfoCollector = mockk(relaxed = true)
        mockInAppDownloader = mockk(relaxed = true)

        json = JsonUtil.json
        mockMetadataReader = mockk(relaxed = true)
        every { mockMetadataReader.getInt(any()) } returns ICON_ID

        pushMessagePresenter = PushMessagePresenter(
            mockContext,
            json,
            mockNotificationManager,
            mockMetadataReader,
            mockNotificationCompatStyler,
            mockPlatformInfoCollector,
            mockInAppDownloader
        )

        notificationSlot = slot<Notification>()

    }

    @After
    fun tearDown() = runTest {
        notificationSlot.clear()
    }

    @Test
    fun present_shouldShowNotification_withCorrectData_withActions() = runTest {
        val testMessage = createTestMessage(testPresentableActions)

        every {
            mockNotificationManager.notify(
                COLLAPSE_ID,
                COLLAPSE_ID.hashCode(),
                capture(notificationSlot)
            )
        } returns Unit

        pushMessagePresenter.present(testMessage)

        assertNotificationFields(notificationSlot.captured)
        notificationSlot.captured.actions.size shouldBe 3
        notificationSlot.captured.actions[0].title shouldBe testCustomEventAction.title
        notificationSlot.captured.actions[1].title shouldBe testAppEventAction.title
        notificationSlot.captured.actions[2].title shouldBe testOpenExternalUrlAction.title

        coVerify {
            mockNotificationCompatStyler.style(
                any<NotificationCompat.Builder>(),
                testMessage
            )
        }
        verify {
            mockNotificationManager.notify(
                COLLAPSE_ID,
                COLLAPSE_ID.hashCode(),
                notificationSlot.captured
            )
        }
    }

    @Test
    fun present_shouldShowNotification_withCorrectData_withoutActions() = runTest {
        val testMessage = createTestMessage(actions = null, defaultTapAction = null)

        every {
            mockNotificationManager.notify(
                COLLAPSE_ID,
                COLLAPSE_ID.hashCode(),
                capture(notificationSlot)
            )
        } returns Unit

        pushMessagePresenter.present(testMessage)

        assertNotificationFields(notificationSlot.captured)
        notificationSlot.captured.actions shouldBe null

        verify {
            mockNotificationManager.notify(
                COLLAPSE_ID,
                COLLAPSE_ID.hashCode(),
                notificationSlot.captured
            )
        }
    }

    @Test
    fun present_shouldShowNotification_withCorrectData_withDefaultActionOnly() = runTest {
        val testMessage = createTestMessage(null, testDefaultTapAction)

        every {
            mockNotificationManager.notify(
                COLLAPSE_ID,
                COLLAPSE_ID.hashCode(),
                capture(notificationSlot)
            )
        } returns Unit

        pushMessagePresenter.present(testMessage)

        assertNotificationFields(notificationSlot.captured)
        notificationSlot.captured.actions shouldBe null

        verify {
            mockNotificationManager.notify(
                COLLAPSE_ID,
                COLLAPSE_ID.hashCode(),
                notificationSlot.captured
            )
        }
    }

    @Test
    fun present_shouldShowNotification_withCorrectData_withActionsAndDefaultAction() = runTest {
        val testMessage = createTestMessage(testPresentableActions, testDefaultTapAction)

        every {
            mockNotificationManager.notify(
                COLLAPSE_ID,
                COLLAPSE_ID.hashCode(),
                capture(notificationSlot)
            )
        } returns Unit

        pushMessagePresenter.present(testMessage)

        assertNotificationFields(notificationSlot.captured)
        notificationSlot.captured.actions.size shouldBe 3

        verify {
            mockNotificationManager.notify(
                COLLAPSE_ID,
                COLLAPSE_ID.hashCode(),
                notificationSlot.captured
            )
        }
    }

    @Test
    fun present_shouldShowNotification_withCorrectData_withBadgeCount() = runTest {
        val testValue = 8
        val testMessage = createTestMessage(
            testPresentableActions,
            testDefaultTapAction,
            BadgeCount(BadgeCountMethod.SET, testValue)
        )

        every {
            mockNotificationManager.notify(
                COLLAPSE_ID,
                COLLAPSE_ID.hashCode(),
                capture(notificationSlot)
            )
        } returns Unit

        pushMessagePresenter.present(testMessage)

        assertNotificationFields(notificationSlot.captured)
        notificationSlot.captured.actions.size shouldBe 3

        verify {
            mockNotificationManager.notify(
                COLLAPSE_ID,
                COLLAPSE_ID.hashCode(),
                notificationSlot.captured
            )
        }

        notificationSlot.captured.number shouldBe testValue
    }

    @Test
    fun present_shouldCreateDebugChannel_if_debugMode_and_channelId_isInvalid() = runTest {
        val message = createTestMessage()
        val testSettings = AndroidNotificationSettings(true, 1, emptyList())

        every { mockPlatformInfoCollector.isDebugMode() } returns true
        every { mockPlatformInfoCollector.notificationSettings() } returns testSettings

        every {
            mockNotificationManager.notify(
                COLLAPSE_ID,
                COLLAPSE_ID.hashCode(),
                capture(notificationSlot)
            )
        } returns Unit

        pushMessagePresenter.present(message)

        notificationSlot.captured.channelId shouldBe DEBUG_CHANNEL_ID
    }

    @Test
    fun present_should_not_CreateDebugChannel_if_debugMode_and_channelId_is_valid() = runTest {
        val message = createTestMessage()
        val channelSettings = listOf(ChannelSettings(CHANNEL_ID))
        val testSettings = AndroidNotificationSettings(true, 1, channelSettings)

        every { mockPlatformInfoCollector.isDebugMode() } returns true
        every { mockPlatformInfoCollector.notificationSettings() } returns testSettings

        every {
            mockNotificationManager.notify(
                COLLAPSE_ID,
                COLLAPSE_ID.hashCode(),
                capture(notificationSlot)
            )
        } returns Unit

        pushMessagePresenter.present(message)

        notificationSlot.captured.channelId shouldBe CHANNEL_ID
    }

    @Test
    fun present_should_not_CreateDebugChannel_if_not_debugMode() = runTest {
        val message = createTestMessage()
        val testSettings = AndroidNotificationSettings(true, 1, emptyList())

        every { mockPlatformInfoCollector.isDebugMode() } returns false
        every { mockPlatformInfoCollector.notificationSettings() } returns testSettings

        every {
            mockNotificationManager.notify(
                COLLAPSE_ID,
                COLLAPSE_ID.hashCode(),
                capture(notificationSlot)
            )
        } returns Unit

        pushMessagePresenter.present(message)

        notificationSlot.captured.channelId shouldBe CHANNEL_ID
    }

    private fun createTestMessage(
        actions: List<PresentableActionModel>? = null,
        defaultTapAction: BasicAppEventActionModel? = null,
        badgeCount: BadgeCount? = null,
        iconUrlString: String? = null,
        imageUrlString: String? = null
    ): AndroidPushMessage {
        val tesMethod = NotificationMethod(COLLAPSE_ID, INIT)
        val testData = PresentablePushData(
            false,
            SID,
            CAMPAIGN_ID,
            AndroidPlatformData(CHANNEL_ID, tesMethod),
            defaultTapAction = defaultTapAction,
            actions = actions,
            badgeCount
        )
        return AndroidPushMessage(
            MESSAGE_ID,
            TITLE,
            BODY,
            iconUrlString,
            imageUrlString,
            data = testData,
        )
    }

    private fun assertNotificationFields(notification: Notification) {
        notification.channelId shouldBe CHANNEL_ID
        notification.smallIcon.resId shouldBe ICON_ID
        notification.contentIntent shouldNotBe null
        notification.extras.getString("android.title") shouldBe TITLE
        notification.extras.getString("android.text") shouldBe BODY
        notification.priority shouldBe NotificationCompat.PRIORITY_DEFAULT
    }
}