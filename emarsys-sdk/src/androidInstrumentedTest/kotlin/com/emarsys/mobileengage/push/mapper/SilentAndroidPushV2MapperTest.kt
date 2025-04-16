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
import kotlinx.serialization.json.put
import org.junit.Before
import org.junit.Test

class SilentAndroidPushV2MapperTest {
    private companion object {
        const val REPORTING = """{"reportingKey":"reportingValue"}"""
    }

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
            put("ems", buildJsonObject {
                put("trackingInfo", buildJsonObject {
                    put("trackingInfoKey", "trackingInfoValue")
                }.toString())
            }.toString())
            put("notification.channelId", "channelId")
            put("notification.collapseId", "collapseKey")
            put("notification.operation", "INIT")
            put("notification.style", "BIG_TEXT")
            put("notification.badgeCount", buildJsonObject {
                put("method", "ADD")
                put("value", 1)
            }.toString())
            put("notification.actions", buildJsonArray {
                add(buildJsonObject {
                    put("type", "MEAppEvent")
                    put("reporting", REPORTING)
                    put("name", "testEvent")
                    put("payload", buildJsonObject {
                        put("key", "value")
                    })
                })
            }.toString())
        }
        val expected = SilentAndroidPushMessage(
            trackingInfo = """{"trackingInfoKey":"trackingInfoValue"}""",
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
                        reporting = REPORTING,
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