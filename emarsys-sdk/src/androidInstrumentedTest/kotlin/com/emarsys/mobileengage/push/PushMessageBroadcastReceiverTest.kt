package com.emarsys.mobileengage.push

import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.emarsys.core.log.SdkLogger
import com.emarsys.di.AndroidPlatformContext
import com.emarsys.di.DependencyContainer
import com.emarsys.di.DependencyInjection
import com.emarsys.mobileengage.action.models.BadgeCount
import com.emarsys.mobileengage.action.models.BadgeCountMethod
import com.emarsys.mobileengage.push.NotificationOperation.INIT
import com.emarsys.mobileengage.push.model.AndroidPlatformData
import com.emarsys.mobileengage.push.model.AndroidPushMessage
import com.emarsys.mobileengage.push.model.NotificationMethod
import com.emarsys.mobileengage.push.model.SilentAndroidPushMessage
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PushMessageBroadcastReceiverTest {
    private companion object {
        const val TITLE = "testTitle"
        const val BODY = "testBody"
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
    private lateinit var mockPresenter: PushMessagePresenter
    private lateinit var mockSilentPushHandler: SilentPushMessageHandler
    private lateinit var mockLogger: SdkLogger
    private lateinit var mockPushMessageFactory: AndroidPushMessageFactory
    private lateinit var sdkDispatcher: CoroutineDispatcher
    private lateinit var broadcastReceiver: PushMessageBroadcastReceiver

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        mockPresenter = mockk(relaxed = true)
        mockSilentPushHandler = mockk(relaxed = true)
        mockLogger = mockk(relaxed = true)
        mockPushMessageFactory = mockk(relaxed = true)
        val mockPlatformContext = mockk<AndroidPlatformContext>(relaxed = true)
        val mockContainer: DependencyContainer = mockk(relaxed = true)
        DependencyInjection.container = mockContainer
        every { mockContainer.platformContext } returns mockPlatformContext
        every { mockPlatformContext.pushMessagePresenter } returns mockPresenter
        every { mockPlatformContext.silentPushHandler } returns mockSilentPushHandler
        every { mockPlatformContext.androidPushMessageFactory } returns mockPushMessageFactory
        every { mockContainer.sdkLogger } returns mockLogger
        sdkDispatcher = StandardTestDispatcher()
        every { mockContainer.sdkDispatcher } returns sdkDispatcher
        every { mockContainer.json } returns Json

        broadcastReceiver =
            PushMessageBroadcastReceiver()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun testOnReceive_shouldGetPushMessageFromIntentAndPresent() = runTest {
        val tesMethod = NotificationMethod(COLLAPSE_ID, INIT)
        val expectedPushMessage = AndroidPushMessage(
            SID,
            CAMPAIGN_ID,
            AndroidPlatformData(CHANNEL_ID, tesMethod),
            null,
            DisplayableData(TITLE, BODY),
            null
        )
        coEvery { mockPushMessageFactory.create(Json.decodeFromString<JsonObject>(
            PUSH_MESSAGE_STRING)) } returns expectedPushMessage

        val intent = Intent().apply {
            action = "com.emarsys.sdk.PUSH_MESSAGE_PAYLOAD"
            putExtra("pushPayload", PUSH_MESSAGE_STRING)
        }

        broadcastReceiver.onReceive(context, intent)

        advanceUntilIdle()

        coVerify(exactly = 0) { mockSilentPushHandler.handle(any()) }
        coVerify { mockPresenter.present(expectedPushMessage) }
    }

    @Test
    fun testOnReceive_shouldHandleSilentPushMessage() = runTest {
        val tesMethod = NotificationMethod(COLLAPSE_ID, INIT)
        val expectedPushMessage = SilentAndroidPushMessage(
            SID,
            CAMPAIGN_ID,
            AndroidPlatformData(CHANNEL_ID, tesMethod),
            BadgeCount(BadgeCountMethod.ADD, 1),
            actionableData = ActionableData()
        )

        coEvery { mockPushMessageFactory.create(Json.decodeFromString(SILENT_PUSH_MESSAGE_STRING)) } returns expectedPushMessage

        val intent = Intent().apply {
            action = "com.emarsys.sdk.PUSH_MESSAGE_PAYLOAD"
            putExtra("pushPayload", SILENT_PUSH_MESSAGE_STRING)
        }

        broadcastReceiver.onReceive(context, intent)

        advanceUntilIdle()

        coVerify { mockSilentPushHandler.handle(expectedPushMessage) }
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

    @Test
    fun testOnReceive_shouldDoNothing_whenPushPayloadKeyIsNotPresent() = runTest {
        val intent = Intent().apply {
            action = "com.emarsys.sdk.PUSH_MESSAGE_PAYLOAD"
            putExtra("notPushPayload", """{"key":"value"}""")
        }

        broadcastReceiver.onReceive(context, intent)

        advanceUntilIdle()

        coVerify { listOf(mockPresenter, mockSilentPushHandler, mockLogger) wasNot Called }
    }


}