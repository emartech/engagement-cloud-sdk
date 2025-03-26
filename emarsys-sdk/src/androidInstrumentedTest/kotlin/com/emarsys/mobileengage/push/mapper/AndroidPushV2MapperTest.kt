package com.emarsys.mobileengage.push.mapper

import com.emarsys.core.log.Logger
import com.emarsys.core.providers.Provider
import com.emarsys.mobileengage.action.models.BadgeCount
import com.emarsys.mobileengage.action.models.BadgeCountMethod
import com.emarsys.mobileengage.action.models.BasicCustomEventActionModel
import com.emarsys.mobileengage.action.models.PresentableAppEventActionModel
import com.emarsys.mobileengage.inapp.PushToInApp
import com.emarsys.mobileengage.push.ActionableData
import com.emarsys.mobileengage.push.DisplayableData
import com.emarsys.mobileengage.push.NotificationOperation
import com.emarsys.mobileengage.push.model.AndroidPlatformData
import com.emarsys.mobileengage.push.model.AndroidPushMessage
import com.emarsys.mobileengage.push.model.NotificationMethod
import com.emarsys.mobileengage.push.model.NotificationStyle
import com.emarsys.util.JsonUtil
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.Before
import org.junit.Test

class AndroidPushV2MapperTest {
    private companion object {
        const val UUID = "testUUID"
        const val SID = "testSid"
        const val CAMPAIGN_ID = "testCampaignId"
    }
    private lateinit var mockUUIDProvider: Provider<String>
    private lateinit var mockLogger: Logger
    private lateinit var mapper: AndroidPushV2Mapper

    @Before
    fun setup() = runTest {
        mockUUIDProvider = mockk(relaxed = true)
        mockLogger = mockk(relaxed = true)
        every { mockUUIDProvider.provide() } returns UUID
        mapper = AndroidPushV2Mapper(mockUUIDProvider, mockLogger, JsonUtil.json)
    }

    @Test
    fun map_shouldReturnNull_whenExceptionThrown() = runTest {
        val input = buildJsonObject {
            put("noNecessaryKeys4U", JsonPrimitive("NO"))
        }

        val result = mapper.map(input)

        result shouldBe null
    }

    @Test
    fun map_shouldReturnAndroidPushMessage_whenInputIsValid() = runTest {
        val input = createTestJson(SID, CAMPAIGN_ID)
        val expected = createExpectedAndroidPushMessage(SID, CAMPAIGN_ID)

        val result = mapper.map(input)

        result shouldBe expected
    }

    @Test
    fun map_shouldReturnAndroidPushMessage_withDefaultSID_whenItsMissingAndLog() = runTest {
        val input = createTestJson(null, CAMPAIGN_ID)
        val expected = createExpectedAndroidPushMessage("missingSID", CAMPAIGN_ID)

        val result = mapper.map(input)

        result shouldBe expected
        coVerify { mockLogger.error(tag = "AndroidPushV2Mapper - extractStringWithFallback", throwable = any<Exception>()) }
    }

    @Test
    fun map_shouldReturnAndroidPushMessage_withDefaultCampaignId_whenItsMissingAndLog() = runTest {
        val input = createTestJson(SID, null)
        val expected = createExpectedAndroidPushMessage(SID, "missingCampaignId")

        val result = mapper.map(input)

        result shouldBe expected
        coVerify { mockLogger.error(tag = "AndroidPushV2Mapper - extractStringWithFallback", throwable = any<Exception>()) }
    }

    private fun createExpectedAndroidPushMessage(sid: String, campaignId: String): AndroidPushMessage = AndroidPushMessage(
        sid = sid,
        campaignId = campaignId,
        platformData = AndroidPlatformData(
            channelId = "channelId",
            notificationMethod = NotificationMethod(
                collapseId = "collapseKey",
                operation = NotificationOperation.INIT
            ),
            style = NotificationStyle.BIG_TEXT
        ),
        badgeCount = BadgeCount(
            method = BadgeCountMethod.ADD,
            value = 1
        ),
        displayableData = DisplayableData(
            title = "title",
            body = "body",
            iconUrlString = "icon",
            imageUrlString = "image"
        ),
        actionableData = ActionableData(
            actions = listOf(
                PresentableAppEventActionModel(
                    title = "testTitle",
                    id = "testId",
                    name = "testEvent",
                    payload = buildMap {
                        put("key", "value")
                    }
                )
            ),
            defaultTapAction = BasicCustomEventActionModel(
                name = "testName",
                payload = buildMap {
                    put("key", "value")
                }
            ),
            pushToInApp = PushToInApp(
                campaignId = "campaignId",
                url = "url",
                ignoreViewedEvent = true
            )
        )
    )

    private fun createTestJson(sid: String?, campaignId: String?): JsonObject {
        val input = buildJsonObject {
            put("ems.version", JsonPrimitive("version"))
            put("ems.treatments", buildJsonObject {
                sid?.let {
                    put("sid", JsonPrimitive(SID))
                }
                campaignId?.let {
                    put("campaignId", JsonPrimitive(CAMPAIGN_ID))
                }
            }.toString())
            put("notification.channelId", JsonPrimitive("channelId"))
            put("notification.collapseId", JsonPrimitive("collapseKey"))
            put("notification.operation", JsonPrimitive("init"))
            put("notification.style", JsonPrimitive("BIG_TEXT"))
            put("notification.badgeCount", JsonPrimitive(buildJsonObject {
                put("method", JsonPrimitive("ADD"))
                put("value", JsonPrimitive(1))
            }.toString()))
            put("notification.title", JsonPrimitive("title"))
            put("notification.body", JsonPrimitive("body"))
            put("notification.icon", JsonPrimitive("icon"))
            put("notification.imageUrl", JsonPrimitive("image"))
            put("notification.actions", JsonPrimitive(buildJsonArray {
                add(buildJsonObject {
                    put("type", JsonPrimitive("MEAppEvent"))
                    put("title", JsonPrimitive("testTitle"))
                    put("id", JsonPrimitive("testId"))
                    put("name", JsonPrimitive("testEvent"))
                    put("payload", buildJsonObject {
                        put("key", JsonPrimitive("value"))
                    })
                })
            }.toString()))
            put("notification.inapp", JsonPrimitive(buildJsonObject {
                put("campaign_id", JsonPrimitive("campaignId"))
                put("url", JsonPrimitive("url"))
                put("ignoreViewedEvent", JsonPrimitive(true))
            }.toString()))
            put("notification.defaultAction", JsonPrimitive(buildJsonObject {
                put("type", JsonPrimitive("MECustomEvent"))
                put("name", JsonPrimitive("testName"))
                put("payload", buildJsonObject {
                    put("key", JsonPrimitive("value"))
                })
            }.toString()))
        }
        return input
    }

}