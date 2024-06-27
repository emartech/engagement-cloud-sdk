package com.emarsys.mobileengage.push

import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.emarsys.core.log.Logger
import com.emarsys.mobileengage.push.model.AndroidPlatformData
import com.emarsys.mobileengage.push.model.AndroidPushMessage
import com.emarsys.mobileengage.push.model.NotificationMethod
import com.emarsys.mobileengage.push.model.NotificationOperation
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
                    "operation":"${NotificationOperation.INIT}"
                    }
                }
            }
        }""".trimIndent()
    }

    private val context = getInstrumentation().targetContext
    private lateinit var mockPresenter: PushPresenter<AndroidPlatformData, AndroidPushMessage>
    private lateinit var mockLogger: Logger
    private lateinit var sdkDispatcher: CoroutineDispatcher
    private lateinit var json: Json
    private lateinit var broadcastReceiver: PushMessageBroadcastReceiver

    @Before
    fun setUp() {
        mockPresenter = mockk(relaxed = true)
        mockLogger = mockk(relaxed = true)
        sdkDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(sdkDispatcher)
        json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
        broadcastReceiver =
            PushMessageBroadcastReceiver(mockPresenter, sdkDispatcher, mockLogger, json)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testOnReceive_shouldGetPushMessageFromIntentAndPresent() = runTest {
        val tesMethod = NotificationMethod(
            COLLAPSE_ID,
            NotificationOperation.INIT
        )
        val testData = PushData(
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

        coVerify { mockPresenter.present(expectedPushMessage) }
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