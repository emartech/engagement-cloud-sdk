package com.emarsys.mobileengage.push

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.emarsys.SdkConstants.PUSH_RECEIVED_EVENT_NAME
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.mobileengage.action.PushActionFactory
import com.emarsys.mobileengage.action.actions.Action
import com.emarsys.mobileengage.action.models.BadgeCount
import com.emarsys.mobileengage.action.models.BasicActionModel
import com.emarsys.mobileengage.action.models.BasicAppEventActionModel
import com.emarsys.mobileengage.action.models.BasicCustomEventActionModel
import com.emarsys.mobileengage.action.models.BasicOpenExternalUrlActionModel
import com.emarsys.mobileengage.inapp.InAppDownloader
import com.emarsys.mobileengage.push.NotificationOperation.INIT
import com.emarsys.mobileengage.push.model.AndroidPlatformData
import com.emarsys.mobileengage.push.model.NotificationMethod
import com.emarsys.mobileengage.push.model.SilentAndroidPushMessage
import com.emarsys.networking.clients.event.model.SdkEvent
import com.emarsys.util.JsonUtil
import dev.mokkery.verifySuspend
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.junit.Before
import org.junit.Test


class PushMessageHandlerTest {
    private companion object {
        const val COLLAPSE_ID = "testCollapseId"
        const val CHANNEL_ID = "testChannelId"
        const val SID = "testSid"
        const val CAMPAIGN_ID = "testCampaignId"

        val testOpenExternalUrlBasicAction =
            BasicOpenExternalUrlActionModel(
                "https://example.com"
            )
        val testCustomEventBasicAction =
            BasicCustomEventActionModel(
                "customAction",
                mapOf("key" to "value")
            )
        val testAppEventBasicAction =
            BasicAppEventActionModel(
                "appAction",
                mapOf("key2" to "value2")
            )

        val testBasicActions = listOf(
            testOpenExternalUrlBasicAction,
            testCustomEventBasicAction,
            testAppEventBasicAction
        )
    }

    private lateinit var silentPushMessageHandler: SilentPushMessageHandler
    private lateinit var mockContext: Context
    private lateinit var json: Json
    private lateinit var mockInAppDownloader: InAppDownloader
    private lateinit var mockPushActionFactory: PushActionFactory
    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi


    @Before
    fun setup() = runTest {
        mockContext = getInstrumentation().targetContext.applicationContext
        mockInAppDownloader = mockk(relaxed = true)
        mockPushActionFactory = mockk(relaxed = true)
        mockSdkEventDistributor = mockk(relaxed = true)

        json = JsonUtil.json

        silentPushMessageHandler = SilentPushMessageHandler(
            mockPushActionFactory,
            mockSdkEventDistributor
        )
    }

    @Test
    fun handle_should_invoke_actions_when_silentPush() = runTest {
        val message = createTestSilentMessage(actions = testBasicActions)
        val mockAction: Action<*> = mockk(relaxed = true)
        coEvery { mockPushActionFactory.create(testCustomEventBasicAction) } returns mockAction
        coEvery { mockPushActionFactory.create(testAppEventBasicAction) } returns mockAction
        coEvery { mockPushActionFactory.create(testOpenExternalUrlBasicAction) } returns mockAction

        silentPushMessageHandler.handle(message)

        coVerify(exactly = 3) { mockAction.invoke() }
    }

    @Test
    fun handle_should_emit_event_with_campaignId() = runTest {
        val message = createTestSilentMessage()

        silentPushMessageHandler.handle(message)

        verifySuspend {
            mockSdkEventDistributor.registerAndStoreEvent(
                SdkEvent.External.Api.SilentPush(
                    name = PUSH_RECEIVED_EVENT_NAME,
                    attributes = buildJsonObject { put("campaignId", JsonPrimitive(CAMPAIGN_ID)) })
            )
        }
    }

    private fun createTestSilentMessage(
        actions: List<BasicActionModel>? = null,
        badgeCount: BadgeCount? = null,
    ): SilentAndroidPushMessage {
        val tesMethod = NotificationMethod(COLLAPSE_ID, INIT)
        return SilentAndroidPushMessage(
            SID,
            CAMPAIGN_ID,
            AndroidPlatformData(CHANNEL_ID, tesMethod),
            badgeCount,
            ActionableData(
                actions = actions
            )
        )
    }
}