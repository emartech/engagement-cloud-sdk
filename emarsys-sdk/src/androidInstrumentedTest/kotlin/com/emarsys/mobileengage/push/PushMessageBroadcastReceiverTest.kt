package com.emarsys.mobileengage.push

import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.emarsys.core.log.Logger
import com.emarsys.mobileengage.action.models.BadgeCount
import com.emarsys.mobileengage.action.models.BadgeCountMethod
import com.emarsys.mobileengage.push.model.AndroidPlatformData
import com.emarsys.mobileengage.push.model.AndroidPushMessage
import com.emarsys.mobileengage.push.model.AndroidSilentPushMessage
import com.emarsys.mobileengage.push.model.NotificationMethod
import com.emarsys.mobileengage.push.model.NotificationOperation.INIT
import com.emarsys.util.JsonUtil
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PushMessageBroadcastReceiverTest {
    private companion object {
        const val TITLE = "testTitle"
        const val BODY = "testBody"
        const val MESSAGE_ID = "testMessageId"
        const val COLLAPSE_ID = "testCollapseId"
        const val CHANNEL_ID = "testChannelId"
        const val SID = "testSid"
        const val CAMPAIGN_ID = "testCampaignId"
        val PUSH_MESSAGE_STRING = """{
        "messageId":"testMessageId",
        "title": "testTitle",
        "body": "testBody",
        "data":{
            "sid":"$SID",
            "campaignId":"$CAMPAIGN_ID",
            "platformData":{
                "channelId":"$CHANNEL_ID",
                "notificationMethod":{
                    "collapseId":"$COLLAPSE_ID",
                    "operation":"$INIT"
                    }
                }
            }
        }""".trimIndent()

        val SILENT_PUSH_MESSAGE_STRING = """{
        "messageId":"testMessageId",
        "data":{
            "silent":true,
            "sid":"$SID",
            "campaignId":"$CAMPAIGN_ID",
            "platformData":{
                "channelId":"$CHANNEL_ID",
                "notificationMethod":{
                    "collapseId":"$COLLAPSE_ID",
                    "operation":"$INIT"
                    }
                },
                "badgeCount":{
                    "method":"ADD",
                    "value":1
                }
            }
        }""".trimIndent()
    }

    private val context = getInstrumentation().targetContext
    private lateinit var mockPresenter: PushPresenter<AndroidPlatformData, AndroidPushMessage>
    private lateinit var mockHandler: PushHandler<AndroidPlatformData, AndroidSilentPushMessage>
    private lateinit var mockLogger: Logger
    private lateinit var sdkDispatcher: CoroutineDispatcher
    private lateinit var json: Json
    private lateinit var broadcastReceiver: PushMessageBroadcastReceiver

    @Before
    fun setUp() {
        mockPresenter = mockk(relaxed = true)
        mockHandler = mockk(relaxed = true)
        mockLogger = mockk(relaxed = true)
        sdkDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(sdkDispatcher)
        json = JsonUtil.json
        broadcastReceiver =
            PushMessageBroadcastReceiver(
                mockPresenter,
                mockHandler,
                sdkDispatcher,
                mockLogger,
                json
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testOnReceive_shouldGetPushMessageFromIntentAndPresent() = runTest {
        val tesMethod = NotificationMethod(COLLAPSE_ID, INIT)
        val testData = PresentablePushData(
            false,
            SID,
            CAMPAIGN_ID, AndroidPlatformData(CHANNEL_ID, tesMethod)
        )
        val expectedPushMessage = AndroidPushMessage(
            MESSAGE_ID,
            TITLE,
            BODY,
            null,
            null,
            data = testData
        )

        val intent = Intent().apply {
            action = "com.emarsys.sdk.PUSH_MESSAGE_PAYLOAD"
            putExtra("pushPayload", PUSH_MESSAGE_STRING)
        }

        broadcastReceiver.onReceive(context, intent)

        advanceUntilIdle()

        coVerify(exactly = 0) { mockHandler.handle(any()) }
        coVerify { mockPresenter.present(expectedPushMessage) }
    }

    @Test
    fun testOnReceive_shouldHandleSilentPushMessage() = runTest {
        val tesMethod = NotificationMethod(COLLAPSE_ID, INIT)
        val testData = PushData(
            true,
            SID,
            CAMPAIGN_ID,
            AndroidPlatformData(CHANNEL_ID, tesMethod),
            actions = null,
            badgeCount = BadgeCount(BadgeCountMethod.ADD, 1)
        )

        val expectedPushMessage = AndroidSilentPushMessage(
            MESSAGE_ID,
            data = testData
        )

        val intent = Intent().apply {
            action = "com.emarsys.sdk.PUSH_MESSAGE_PAYLOAD"
            putExtra("pushPayload", SILENT_PUSH_MESSAGE_STRING)
        }

        broadcastReceiver.onReceive(context, intent)

        advanceUntilIdle()

        coVerify { mockHandler.handle(expectedPushMessage) }
        coVerify(exactly = 0) { mockPresenter.present(any()) }
    }

    @Test
    fun testOnReceive_shouldLogError_whenPushPayload_couldNotBeDecoded() = runTest {
        val intent = Intent().apply {
            action = "com.emarsys.sdk.PUSH_MESSAGE_PAYLOAD"
            putExtra("pushPayload", """{"key":"value"}""")
        }

        broadcastReceiver.onReceive(context, intent)

        advanceUntilIdle()

        coVerify { mockLogger.error(eq("PushMessageBroadcastReceiver"), any<Exception>()) }
        coVerify(exactly = 0) { mockPresenter.present(any()) }
    }
}