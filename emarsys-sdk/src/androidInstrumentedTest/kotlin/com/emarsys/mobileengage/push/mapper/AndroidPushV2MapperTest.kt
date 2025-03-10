package com.emarsys.mobileengage.push.mapper

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
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import org.junit.Before
import org.junit.Test

class AndroidPushV2MapperTest {

    private lateinit var mapper: AndroidPushV2Mapper

    @Before
    fun setup() = runTest {
        mapper = AndroidPushV2Mapper(mockk(relaxed = true), JsonUtil.json)
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
        val input = buildJsonObject {
            put("ems", JsonPrimitive(buildJsonObject {
                put("treatments", buildJsonObject {
                    put("sid", JsonPrimitive("sid"))
                })
                put("campaignId", JsonPrimitive("campaignId"))
            }.toString()))
            put("notification.channelId", JsonPrimitive("channelId"))
            put("notification.collapseId", JsonPrimitive("collapseKey"))
            put("notification.operation", JsonPrimitive("INIT"))
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
        val expected = AndroidPushMessage(
            sid = "sid",
            campaignId = "campaignId",
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

        val result = mapper.map(input)

        result shouldBe expected
    }

}