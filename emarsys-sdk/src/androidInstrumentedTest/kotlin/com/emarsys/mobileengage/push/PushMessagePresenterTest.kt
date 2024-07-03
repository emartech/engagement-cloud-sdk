package com.emarsys.mobileengage.push

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.emarsys.core.resource.MetadataReader
import com.emarsys.mobileengage.action.models.BasicAppEventActionModel
import com.emarsys.mobileengage.action.models.PresentableActionModel
import com.emarsys.mobileengage.action.models.PresentableAppEventActionModel
import com.emarsys.mobileengage.action.models.PresentableCustomEventActionModel
import com.emarsys.mobileengage.action.models.PresentableOpenExternalUrlActionModel
import com.emarsys.mobileengage.push.model.AndroidPlatformData
import com.emarsys.mobileengage.push.model.AndroidPushMessage
import com.emarsys.mobileengage.push.model.NotificationMethod
import com.emarsys.mobileengage.push.model.NotificationOperation.INIT
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
        val testActions =
            listOf(testCustomEventAction, testAppEventAction, testOpenExternalUrlAction)
        val testDefaultTapAction = BasicAppEventActionModel("testName", null)
    }

    private lateinit var pushMessagePresenter: PushMessagePresenter
    private lateinit var mockContext: Context
    private lateinit var mockNotificationManager: NotificationManager
    private lateinit var json: Json
    private lateinit var mockMetadataReader: MetadataReader
    private lateinit var mockNotificationCompatStyler: NotificationCompatStyler
    private lateinit var notificationSlot: CapturingSlot<Notification>

    @Before
    fun setup() = runTest {
        mockContext = getInstrumentation().targetContext.applicationContext
        mockNotificationManager = mockk(relaxed = true)
        mockNotificationCompatStyler = mockk(relaxed = true)

        json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
        mockMetadataReader = mockk(relaxed = true)
        every { mockMetadataReader.getInt(any()) } returns ICON_ID

        pushMessagePresenter = PushMessagePresenter(
            mockContext,
            json,
            mockNotificationManager,
            mockMetadataReader,
            mockNotificationCompatStyler
        )

        notificationSlot = slot<Notification>()

    }

    @After
    fun tearDown() = runTest {
        notificationSlot.clear()
    }

    @Test
    fun present_shouldShowNotification_withCorrectData_withActions() = runTest {
        val testMessage = createTestMessage(testActions)

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
        val testMessage = createTestMessage(testActions, testDefaultTapAction)

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

    private fun createTestMessage(
        actions: List<PresentableActionModel>? = null,
        defaultTapAction: BasicAppEventActionModel? = null,
        iconUrlString: String? = null,
        imageUrlString: String? = null
    ): AndroidPushMessage {
        val tesMethod = NotificationMethod(COLLAPSE_ID, INIT)
        val testData = PushData(
            false,
            SID,
            CAMPAIGN_ID,
            AndroidPlatformData(CHANNEL_ID, tesMethod),
            defaultTapAction = defaultTapAction,
            actions = actions
        )
        return AndroidPushMessage(
            MESSAGE_ID,
            TITLE,
            BODY,
            iconUrlString,
            imageUrlString,
            data = testData
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