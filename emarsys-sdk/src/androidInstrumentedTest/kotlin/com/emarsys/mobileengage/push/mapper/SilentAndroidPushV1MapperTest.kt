package com.emarsys.mobileengage.push.mapper

import com.emarsys.mobileengage.action.models.BadgeCount
import com.emarsys.mobileengage.action.models.BadgeCountMethod
import com.emarsys.mobileengage.action.models.BasicAppEventActionModel
import com.emarsys.mobileengage.push.ActionableData
import com.emarsys.mobileengage.push.NotificationOperation
import com.emarsys.mobileengage.push.model.AndroidPlatformData
import com.emarsys.mobileengage.push.model.NotificationMethod
import com.emarsys.mobileengage.push.model.NotificationStyle
import com.emarsys.mobileengage.push.model.SilentAndroidPushMessage
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import org.junit.Before
import org.junit.Test

class SilentAndroidPushV1MapperTest {

    private lateinit var mapper: SilentAndroidPushV1Mapper

    @Before
    fun setup() = runTest {
        mapper = SilentAndroidPushV1Mapper(mockk(relaxed = true))
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
    fun map_shouldReturnSilentPushMessage_whenInputIsValid() = runTest {
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
            put("ems.actions", JsonPrimitive(buildJsonArray {
                add(buildJsonObject {
                    put("type", JsonPrimitive("MEAppEvent"))
                    put("name", JsonPrimitive("testEvent"))
                    put("payload", buildJsonObject {
                        put("key", JsonPrimitive("value"))
                    })
                })
            }.toString()))
        }
        val expected = SilentAndroidPushMessage(
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
            actionableData = ActionableData(
                actions = listOf(
                    BasicAppEventActionModel(
                        name = "testEvent",
                        payload = buildMap {
                            put("key", "value")
                        }
                    )
                )
            )
        )

        val result = mapper.map(input)

        result shouldBe expected
    }

}