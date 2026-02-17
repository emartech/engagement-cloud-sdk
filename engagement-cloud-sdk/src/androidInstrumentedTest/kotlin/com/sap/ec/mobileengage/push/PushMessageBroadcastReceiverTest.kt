package com.sap.ec.mobileengage.push

import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.sap.ec.core.log.Logger
import com.sap.ec.core.log.SdkLogger
import com.sap.ec.di.DispatcherTypes
import com.sap.ec.di.SdkKoinIsolationContext.koin
import com.sap.ec.mobileengage.action.models.BadgeCount
import com.sap.ec.mobileengage.action.models.BadgeCountMethod
import com.sap.ec.mobileengage.push.NotificationOperation.INIT
import com.sap.ec.mobileengage.push.model.AndroidPlatformData
import com.sap.ec.mobileengage.push.model.AndroidPushMessage
import com.sap.ec.mobileengage.push.model.NotificationMethod
import com.sap.ec.mobileengage.push.model.SilentAndroidPushMessage
import com.sap.ec.util.JsonUtil
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
        const val TRACKING_INFO = """{"trackingInfoKey":"trackingInfoValue"}"""
        val PUSH_MESSAGE_STRING = """{
        "messageId":"testMessageId",
        "title": "testTitle",
        "body": "testBody",
        "data":{
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
            TRACKING_INFO,
            AndroidPlatformData(CHANNEL_ID, tesMethod),
            null,
            DisplayableData(TITLE, BODY),
            null
        )
        val expectedSilentPushMessage = SilentAndroidPushMessage(
            TRACKING_INFO,
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
        mockLogger = mockk(relaxed = true)
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
            action = "com.sap.ec.sdk.PUSH_MESSAGE_PAYLOAD"
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
            action = "com.sap.ec.sdk.PUSH_MESSAGE_PAYLOAD"
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
            action = "com.sap.ec.sdk.PUSH_MESSAGE_PAYLOAD"
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
            action = "com.sap.ec.sdk.PUSH_MESSAGE_PAYLOAD"
            putExtra("notPushPayload", """{"key":"value"}""")
        }

        broadcastReceiver.onReceive(context, intent)

        advanceUntilIdle()

        coVerify { listOf(mockPresenter, mockSilentPushHandler) wasNot Called }
    }


}