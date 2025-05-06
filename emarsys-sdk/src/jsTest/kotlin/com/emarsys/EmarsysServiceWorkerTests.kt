package com.emarsys

import com.emarsys.api.push.PushConstants.WEB_PUSH_ON_BADGE_COUNT_UPDATE_RECEIVED
import com.emarsys.core.log.ConsoleLogger
import com.emarsys.core.mapper.Mapper
import com.emarsys.mobileengage.action.models.BadgeCount
import com.emarsys.mobileengage.action.models.BadgeCountMethod
import com.emarsys.mobileengage.push.DisplayableData
import com.emarsys.mobileengage.push.PushMessagePresenter
import com.emarsys.mobileengage.push.model.JsPlatformData
import com.emarsys.mobileengage.push.model.JsPushMessage
import com.emarsys.util.JsonUtil
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.StringFormat
import kotlinx.serialization.encodeToString
import web.broadcast.BroadcastChannel
import kotlin.test.BeforeTest
import kotlin.test.Test


class EmarsysServiceWorkerTests {

    companion object {
        const val TEST_PUSH_MESSAGE_STRING = "testPushMessage"
    }

    private lateinit var mockPushMessagePresenter: PushMessagePresenter
    private lateinit var mockPushMessageWebV1Mapper: Mapper<String, JsPushMessage>
    private lateinit var mockJson: StringFormat
    private lateinit var onBadgeCountUpdateReceivedBroadcastChannel: BroadcastChannel
    private lateinit var emarsysServiceWorker: EmarsysServiceWorker

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        mockPushMessagePresenter = mock()
        mockJson = mock()

        mockPushMessageWebV1Mapper = mock()
        onBadgeCountUpdateReceivedBroadcastChannel =
            BroadcastChannel(WEB_PUSH_ON_BADGE_COUNT_UPDATE_RECEIVED)

        emarsysServiceWorker = EmarsysServiceWorker(
            mockPushMessagePresenter,
            mockPushMessageWebV1Mapper,
            onBadgeCountUpdateReceivedBroadcastChannel,
            mockJson,
            TestScope(SupervisorJob()),
            ConsoleLogger()
        )
    }

    @Test
    fun onPush_shouldReturnNullAndNotCallPresent_whenPushMessageIsInvalid() = runTest {
        val invalidPushMessage = "invalidPushMessage"
        everySuspend { mockPushMessageWebV1Mapper.map(invalidPushMessage) } returns null

        val result = emarsysServiceWorker.onPush(invalidPushMessage).await()

        result shouldBe null
        verifySuspend(VerifyMode.exactly(0)) {
            mockPushMessagePresenter.present(any())
        }
    }

    @Test
    fun onPush_shouldCallPresent_whenPushMessageIsValid_forPushMessageWebV1Mapper() = runTest {
        val testJsPushMessage = getJsPushMessage()
        everySuspend { mockPushMessageWebV1Mapper.map(TEST_PUSH_MESSAGE_STRING) } returns testJsPushMessage
        everySuspend { mockPushMessagePresenter.present(any()) } returns Unit

        emarsysServiceWorker.onPush(TEST_PUSH_MESSAGE_STRING).await()

        verifySuspend(VerifyMode.exactly(1)) {
            mockPushMessagePresenter.present(testJsPushMessage)
        }
    }

    @Test
    fun onPush_shouldSerializeBadgeCount_whenPushMessageIsValid_andBadgeCountIsPresent(): TestResult =
        runTest {
            val testBadgeCount = BadgeCount(BadgeCountMethod.ADD, 1)
            val testJsPushMessage = getJsPushMessage(testBadgeCount)
            everySuspend { mockPushMessageWebV1Mapper.map(TEST_PUSH_MESSAGE_STRING) } returns testJsPushMessage
            everySuspend { mockPushMessagePresenter.present(any()) } returns Unit
            everySuspend { mockJson.serializersModule } returns JsonUtil.json.serializersModule
            everySuspend {
                mockJson.encodeToString(
                    BadgeCount.serializer(),
                    testBadgeCount
                )
            } returns "testBadgeCountString"

            emarsysServiceWorker.onPush(TEST_PUSH_MESSAGE_STRING).await()

            verifySuspend(VerifyMode.exactly(1)) {
                mockJson.encodeToString(testBadgeCount)
                mockPushMessagePresenter.present(testJsPushMessage)
            }
        }

    private fun getJsPushMessage(badgeCount: BadgeCount? = null) = JsPushMessage(
        trackingInfo = """{"trackingInfoKey":"trackingInfoValue"}""",
        platformData = JsPlatformData,
        badgeCount = badgeCount,
        actionableData = null,
        displayableData = DisplayableData(
            "title",
            "message",
            "icon",
            "image"
        )
    )

}