package com.sap.ec.mobileengage.push.mapper

import com.sap.ec.core.log.Logger
import com.sap.ec.core.providers.UuidProviderApi
import com.sap.ec.mobileengage.action.models.BadgeCount
import com.sap.ec.mobileengage.action.models.BadgeCountMethod
import com.sap.ec.mobileengage.action.models.BasicActionModel
import com.sap.ec.mobileengage.action.models.BasicAppEventActionModel
import com.sap.ec.mobileengage.push.ActionableData
import com.sap.ec.mobileengage.push.NotificationOperation
import com.sap.ec.mobileengage.push.model.AndroidPlatformData
import com.sap.ec.mobileengage.push.model.NotificationMethod
import com.sap.ec.mobileengage.push.model.NotificationStyle
import com.sap.ec.mobileengage.push.model.SilentAndroidPushMessage
import com.sap.ec.util.JsonUtil
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import org.junit.Before
import org.junit.Test

class SilentHuaweiPushV2MapperTest {

    private companion object {
        const val UUID = "testUUID"
        const val TRACKING_INFO = """{"trackingInfoKey":"trackingInfoValue"}"""
        const val REPORTING = """{"reportingKey":"reportingValue"}"""
        const val CHANNEL_ID = "testChannelId"
        const val COLLAPSE_ID = "testCollapseId"
    }

    private lateinit var mockUUIDProvider: UuidProviderApi
    private lateinit var mockLogger: Logger
    private lateinit var mapper: SilentHuaweiPushV2Mapper

    @Before
    fun setup() = runTest {
        mockUUIDProvider = mockk(relaxed = true)
        mockLogger = mockk(relaxed = true)

        every { mockUUIDProvider.provide() } returns UUID
        mapper = SilentHuaweiPushV2Mapper(mockUUIDProvider, mockLogger, JsonUtil.json)
    }

    @Test
    fun map_shouldReturnNull_andLogError_whenExceptionThrown() = runTest {
        val input = buildJsonObject {
            put("notPushPayload", JsonPrimitive("NO"))
        }

        val result = mapper.map(input)

        result shouldBe null
        coVerify { mockLogger.error(any<String>(), any<Throwable>()) }
    }

    @Test
    fun map_shouldReturnSilentAndroidPushMessage_whenInputIsValid() = runTest {
        val input = buildJsonObject {
            putJsonObject("notification") {
                put("collapseId", COLLAPSE_ID)
                put("channelId", CHANNEL_ID)
                put("operation", "init")
                put("style", "MESSAGE")
                put("actions", buildJsonArray {
                    add(buildJsonObject {
                        put("type", "MEAppEvent")
                        put("reporting", REPORTING)
                        put("name", "testEvent")
                        put("payload", buildJsonObject {
                            put("key", "value")
                        })
                    })
                })
                putJsonObject("badgeCount") {
                    put("method", "ADD")
                    put("value", 3)
                }
            }
            putJsonObject("ems") {
                put("version", "HUAWEI_V2")
                put("trackingInfo", TRACKING_INFO)
                put("rootParams", buildJsonObject {
                    put("anyKey", "anyValue")
                })
                put("customData", buildJsonObject {
                    put("anyDataKey", "anyDataValue")
                })
            }
        }

        val platformData = AndroidPlatformData(
            CHANNEL_ID,
            notificationMethod = NotificationMethod(
                COLLAPSE_ID,
                operation = NotificationOperation.INIT
            ),
            NotificationStyle.MESSAGE
        )

        val badgeCount = BadgeCount(
            BadgeCountMethod.ADD,
            3
        )

        val actionableData = ActionableData(
            actions = listOf<BasicActionModel>(
                BasicAppEventActionModel(
                    reporting = REPORTING,
                    name = "testEvent",
                    payload = buildMap {
                        put("key", "value")
                    }
                )
            )
        )

        val expectedOutput = SilentAndroidPushMessage(
            TRACKING_INFO,
            platformData,
            badgeCount,
            actionableData
        )

        val result = mapper.map(input)

        result shouldBe expectedOutput
    }
}
