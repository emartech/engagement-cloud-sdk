package com.sap.ec.mobileengage.push.mapper

import com.sap.ec.core.log.Logger
import com.sap.ec.core.providers.UuidProviderApi
import com.sap.ec.mobileengage.action.models.BadgeCount
import com.sap.ec.mobileengage.action.models.BadgeCountMethod
import com.sap.ec.mobileengage.action.models.BasicCustomEventActionModel
import com.sap.ec.mobileengage.action.models.PresentableActionModel
import com.sap.ec.mobileengage.action.models.PresentableAppEventActionModel
import com.sap.ec.mobileengage.push.ActionableData
import com.sap.ec.mobileengage.push.DisplayableData
import com.sap.ec.mobileengage.push.NotificationOperation
import com.sap.ec.mobileengage.push.model.AndroidPlatformData
import com.sap.ec.mobileengage.push.model.AndroidPushMessage
import com.sap.ec.mobileengage.push.model.NotificationMethod
import com.sap.ec.mobileengage.push.model.NotificationStyle
import com.sap.ec.util.JsonUtil
import io.kotest.matchers.shouldBe
import io.mockk.CapturingSlot
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import org.junit.Before
import org.junit.Test


private const val BODY = "testBody"


class HuaweiPushV2MapperTest {

    private companion object {
        const val UUID = "testUUID"
        const val TRACKING_INFO = """{"trackingInfoKey":"trackingInfoValue"}"""
        const val REPORTING = """{"reportingKey":"reportingValue"}"""
        const val ID = "testId"
        const val CHANNEL_ID = "testChannelId"
        const val COLLAPSE_ID = "testCollapseId"
        const val TITLE = "testOfTest"
        const val ICON = "testIcon"
        const val IMAGE_URL = "testImageUrl"

    }

    private lateinit var mockUUIDProvider: UuidProviderApi
    private lateinit var mockLogger: Logger
    private lateinit var mapper: HuaweiPushV2Mapper
    private lateinit var exceptionSlot: CapturingSlot<Throwable>

    @Before
    fun setup() = runTest {
        exceptionSlot = slot<Throwable>()
        mockUUIDProvider = mockk(relaxed = true)
        mockLogger = mockk(relaxed = true)
        coEvery { mockLogger.error(any<String>(), capture(exceptionSlot)) } returns Unit

        every { mockUUIDProvider.provide() } returns UUID
        mapper = HuaweiPushV2Mapper(mockUUIDProvider, mockLogger, JsonUtil.json)
    }

    @Test
    fun map_shouldReturnNull_andLogError_whenExceptionThrown() = runTest {
        val input = buildJsonObject {
            put("noNecessaryKeys4U", JsonPrimitive("NO"))
        }

        val result = mapper.map(input)

        result shouldBe null
        coVerify { mockLogger.error(any<String>(), any<Throwable>()) }
    }

    @Test
    fun map_shouldReturnNull_andLogError_whenEms_objectIsMissing() = runTest {

        val input = buildJsonObject {
            put("notification", JsonObject(mapOf("key" to JsonPrimitive("value"))))
        }

        val result = mapper.map(input)

        result shouldBe null
        exceptionSlot.captured.message?.contains("ems object missing") shouldBe true
    }

    @Test
    fun map_shouldReturnNull_andLogError_whenNotification_objectIsMissing() = runTest {
        val input = buildJsonObject {
            put("ems", JsonObject(mapOf("key" to JsonPrimitive("value"))))
        }

        val result = mapper.map(input)

        result shouldBe null
        exceptionSlot.captured.message?.contains("notification object missing") shouldBe true
    }

    @Test
    fun map_shouldReturnNull_whenTitleIsNull() = runTest {
        val input = createTestInput(title = null)

        val result = mapper.map(input)

        result shouldBe null
    }

    @Test
    fun map_shouldReturnNull_whenBodyIsNull() = runTest {
        val input = createTestInput(body = null)

        val result = mapper.map(input)

        result shouldBe null
    }

    @Test
    fun map_shouldReturnAndroidPushMessage_whenInputIsValid() = runTest {
        val input = createTestInput()
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

        val displayableData = DisplayableData(
            TITLE,
            BODY,
            ICON,
            IMAGE_URL
        )

        val actionableData: ActionableData<PresentableActionModel> = ActionableData(
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
            defaultTapAction = BasicCustomEventActionModel(
                reporting = REPORTING,
                name = "testName",
                payload = buildMap {
                    put("key", "value")
                }
            ))


        val expectedOutput = AndroidPushMessage(
            TRACKING_INFO,
            platformData,
            badgeCount,
            displayableData,
            actionableData
        )

        val result = mapper.map(input)

        result shouldBe expectedOutput
    }

    private fun createTestInput(title: String? = TITLE, body: String? = BODY): JsonObject {
        return buildJsonObject {
            putJsonObject("notification") {
                put("silent", false)
                put("title", title)
                put("body", body)
                put("icon", ICON)
                put("imageUrl", IMAGE_URL)
                put("style", "MESSAGE")
                put("collapseId", COLLAPSE_ID)
                put("channelId", CHANNEL_ID)
                put("operation", "init")
                put("defaultAction", buildJsonObject {
                    put("type", "MECustomEvent")
                    put("reporting", REPORTING)
                    put("name", "testName")
                    put("payload", buildJsonObject {
                        put("key", "value")
                    })
                })
                put("actions", buildJsonArray {
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

    }
}

