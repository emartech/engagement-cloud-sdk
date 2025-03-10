package com.emarsys.mobileengage.push.mapper

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
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import org.junit.Before
import org.junit.Test

class AndroidPushV1MapperTest {

    private lateinit var uuidProvider: Provider<String>
    private lateinit var mapper: AndroidPushV1Mapper

    @Before
    fun setup() = runTest {
        uuidProvider = mockk(relaxed = true)
        mapper = AndroidPushV1Mapper(mockk(relaxed = true), JsonUtil.json, mockk(relaxed = true))
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
            put("ems.sid", JsonPrimitive("sid"))
            put("ems.multichannel_id", JsonPrimitive("campaignId"))
            put("notification.channel_id", JsonPrimitive("channelId"))
            put("ems.notification_method.collapse_key", JsonPrimitive("collapseKey"))
            put("ems.notification_method.operation", JsonPrimitive("INIT"))
            put("ems.style", JsonPrimitive("BIG_TEXT"))
            put("notification.badgeCount", JsonPrimitive(buildJsonObject {
                put("method", JsonPrimitive("ADD"))
                put("value", JsonPrimitive(1))
            }.toString()))
            put("notification.title", JsonPrimitive("title"))
            put("notification.body", JsonPrimitive("body"))
            put("notification.icon", JsonPrimitive("icon"))
            put("notification.image", JsonPrimitive("image"))
            put("ems.actions", JsonPrimitive(buildJsonArray {
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
            put("ems.inapp", JsonPrimitive(buildJsonObject {
                put("campaign_id", JsonPrimitive("campaignId"))
                put("url", JsonPrimitive("url"))
                put("ignoreViewedEvent", JsonPrimitive(true))
            }.toString()))
            put("ems.tap_actions.default_action.type", JsonPrimitive("MECustomEvent"))
            put("ems.tap_actions.default_action.name", JsonPrimitive("testName"))
            put("ems.tap_actions.default_action.payload", JsonPrimitive(buildJsonObject {
                put("key", JsonPrimitive("value"))
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