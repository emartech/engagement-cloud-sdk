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
import com.emarsys.util.JsonUtil
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import org.junit.Before
import org.junit.Test

class SilentAndroidPushV2MapperTest {

    private lateinit var mapper: SilentAndroidPushV2Mapper

    @Before
    fun setup() = runTest {
        mapper = SilentAndroidPushV2Mapper(mockk(relaxed = true), JsonUtil.json)
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
            put("notification.actions", JsonPrimitive(buildJsonArray {
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