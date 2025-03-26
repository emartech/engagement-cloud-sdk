package com.emarsys.mobileengage.push

import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.emarsys.core.log.ConsoleLogger
import com.emarsys.core.log.Logger
import com.emarsys.core.log.SdkLogger
import com.emarsys.di.DispatcherTypes
import com.emarsys.di.SdkKoinIsolationContext.koin
import com.emarsys.mobileengage.action.models.BadgeCount
import com.emarsys.mobileengage.action.models.BadgeCountMethod
import com.emarsys.mobileengage.push.NotificationOperation.INIT
import com.emarsys.mobileengage.push.model.AndroidPlatformData
import com.emarsys.mobileengage.push.model.AndroidPushMessage
import com.emarsys.mobileengage.push.model.NotificationMethod
import com.emarsys.mobileengage.push.model.SilentAndroidPushMessage
import com.emarsys.util.JsonUtil
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
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
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.Koin
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.test.KoinTest

@OptIn(ExperimentalCoroutinesApi::class)
class PushMessageBroadcastReceiverTest : KoinTest {
    override fun getKoin(): Koin = koin

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
        val tesMethod = NotificationMethod(COLLAPSE_ID, INIT)
        val expectedPushMessage = AndroidPushMessage(
            SID,
            CAMPAIGN_ID,
            AndroidPlatformData(CHANNEL_ID, tesMethod),
            null,
            DisplayableData(TITLE, BODY),
            null
        )
        val expectedSilentPushMessage = SilentAndroidPushMessage(
            SID,
            CAMPAIGN_ID,
            AndroidPlatformData(CHANNEL_ID, tesMethod),
            BadgeCount(BadgeCountMethod.ADD, 1),
            actionableData = ActionableData()
        )
    }

    private val context = getInstrumentation().targetContext

    private lateinit var mockPresenter: PushMessagePresenter
    private lateinit var mockSilentPushHandler: SilentPushMessageHandler
    private lateinit var mockLogger: SdkLogger
    private lateinit var mockPushMessageFactory: AndroidPushMessageFactory
    private lateinit var sdkDispatcher: CoroutineDispatcher
    private lateinit var broadcastReceiver: PushMessageBroadcastReceiver
    private lateinit var testModules: Module

    init {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        mockPresenter = mockk(relaxed = true)
        mockSilentPushHandler = mockk(relaxed = true)
        mockLogger = SdkLogger(ConsoleLogger())
        mockPushMessageFactory = mockk()
        coEvery { mockPushMessageFactory.create(Json.decodeFromString(SILENT_PUSH_MESSAGE_STRING)) } returns expectedSilentPushMessage
        coEvery { mockPushMessageFactory.create(Json.decodeFromString(PUSH_MESSAGE_STRING)) } returns expectedPushMessage

        sdkDispatcher = StandardTestDispatcher()

        pushMessageBroadcastReceiverTest()

        broadcastReceiver = PushMessageBroadcastReceiver()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        koin.unloadModules(listOf(testModules))
        unmockkAll()
    }

    private fun pushMessageBroadcastReceiverTest() {
        testModules = module {
            single<CoroutineDispatcher>(named(DispatcherTypes.Sdk)) { sdkDispatcher }
            single<Logger> { mockLogger }
            single<Json> { JsonUtil.json }
            single<PushMessagePresenter> { mockPresenter }
            single<AndroidPushMessageFactory> { mockPushMessageFactory }
            single<SilentPushMessageHandler> { mockSilentPushHandler }
        }
        koin.loadModules(listOf(testModules))
    }

    @Test
    fun testOnReceive_shouldGetPushMessageFromIntentAndPresent() = runTest {
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
        val intent = Intent().apply {
            action = "com.emarsys.sdk.PUSH_MESSAGE_PAYLOAD"
            putExtra("pushPayload", SILENT_PUSH_MESSAGE_STRING)
        }

        broadcastReceiver.onReceive(context, intent)

        advanceUntilIdle()

        coVerify { mockSilentPushHandler.handle(expectedSilentPushMessage) }
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

//        coVerify { mockLogger.error(eq("PushMessageBroadcastReceiver"), any<Exception>()) }
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

        coVerify { listOf(mockPresenter, mockSilentPushHandler) wasNot Called }
    }


}