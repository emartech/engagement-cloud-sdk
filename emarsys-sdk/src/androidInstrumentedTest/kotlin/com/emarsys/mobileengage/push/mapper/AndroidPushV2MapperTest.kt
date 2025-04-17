package com.emarsys.mobileengage.push.mapper

import com.emarsys.core.log.Logger
import com.emarsys.core.providers.UuidProviderApi
import com.emarsys.mobileengage.action.models.BadgeCount
import com.emarsys.mobileengage.action.models.BadgeCountMethod
import com.emarsys.mobileengage.action.models.BasicActionModel
import com.emarsys.mobileengage.action.models.BasicCustomEventActionModel
import com.emarsys.mobileengage.action.models.BasicPushToInAppActionModel
import com.emarsys.mobileengage.action.models.PresentableAppEventActionModel
import com.emarsys.mobileengage.inapp.PushToInAppPayload
import com.emarsys.mobileengage.push.ActionableData
import com.emarsys.mobileengage.push.DisplayableData
import com.emarsys.mobileengage.push.NotificationOperation
import com.emarsys.mobileengage.push.model.AndroidPlatformData
import com.emarsys.mobileengage.push.model.AndroidPushMessage
import com.emarsys.mobileengage.push.model.NotificationMethod
import com.emarsys.mobileengage.push.model.NotificationStyle
import com.emarsys.util.JsonUtil
import io.kotest.matchers.shouldBe
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
        const val TRACKING_INFO = """{"trackingInfoKey":"trackingInfoValue"}"""
        const val REPORTING = """{"reportingKey":"reportingValue"}"""
        const val ID = "testId"
    }

    private lateinit var mockUUIDProvider: UuidProviderApi
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
        val input = createTestJson()
        val expected = createExpectedAndroidPushMessage()

        val result = mapper.map(input)

        result shouldBe expected
    }

    @Test
    fun map_shouldReturnAndroidPushMessage_whenInputIsValid_withPushToInApp_defaultAction() =
        runTest {
            val input = createTestJson(buildJsonObject {
                put("type", "InApp")
                put("id", "testId")
                put("reporting", "{\"reportingKey\":\"reportingValue\"}")
                put("payload", buildJsonObject {
                    put("campaignId", "testCampaignId")
                    put("url", "testUrl")
                })
            })
            val expected = createExpectedAndroidPushMessage(
                BasicPushToInAppActionModel(
                    id = "testId",
                    reporting = REPORTING,
                    payload = PushToInAppPayload("testCampaignId", "testUrl")
                )
            )

            val result = mapper.map(input)

            result shouldBe expected
        }

    private fun createExpectedAndroidPushMessage(
        defaultActionModel: BasicActionModel = BasicCustomEventActionModel(
            reporting = REPORTING,
            name = "testName",
            payload = buildMap {
                put("key", "value")
            }
        )
    ): AndroidPushMessage = AndroidPushMessage(
        trackingInfo = TRACKING_INFO,
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
                    id = ID,
                    title = "testTitle",
                    reporting = REPORTING,
                    name = "testEvent",
                    payload = buildMap {
                        put("key", "value")
                    }
                )
            ),
            defaultTapAction = defaultActionModel)
    )

    private fun createTestJson(
        defaultActionModel: JsonObject? = buildJsonObject {
            put("type", "MECustomEvent")
            put("reporting", REPORTING)
            put("name", "testName")
            put("payload", buildJsonObject {
                put("key", "value")
            })
        }
    ): JsonObject {
        val input = buildJsonObject {
            put("ems.version", "version")
            put("ems.trackingInfo", TRACKING_INFO)
            put("notification.channelId", "channelId")
            put("notification.collapseId", "collapseKey")
            put("notification.operation", "init")
            put("notification.style", "BIG_TEXT")
            put("notification.badgeCount", buildJsonObject {
                put("method", "ADD")
                put("value", 1)
            }.toString())
            put("notification.title", "title")
            put("notification.body", "body")
            put("notification.icon", "icon")
            put("notification.imageUrl", "image")
            put("notification.actions", buildJsonArray {
                add(buildJsonObject {
                    put("type", "MEAppEvent")
                    put("id", ID)
                    put("title", "testTitle")
                    put("reporting", REPORTING)
                    put("name", "testEvent")
                    put("payload", buildJsonObject {
                        put("key", "value")
                    })
                })
            }.toString())
            put("notification.defaultAction", defaultActionModel.toString())
        }
        return input
    }

}