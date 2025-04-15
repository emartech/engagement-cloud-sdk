package com.emarsys.mobileengage.push.mappers

import com.emarsys.core.log.Logger
import com.emarsys.mobileengage.action.models.BadgeCount
import com.emarsys.mobileengage.action.models.BadgeCountMethod
import com.emarsys.mobileengage.action.models.BasicCustomEventActionModel
import com.emarsys.mobileengage.action.models.PresentableAppEventActionModel
import com.emarsys.mobileengage.push.ActionableData
import com.emarsys.mobileengage.push.DisplayableData
import com.emarsys.mobileengage.push.model.JsPlatformData
import com.emarsys.mobileengage.push.model.JsPushMessage
import com.emarsys.util.JsonUtil
import dev.mokkery.MockMode
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import kotlin.test.BeforeTest
import kotlin.test.Test

class PushMessageWebV2MapperTests {
    private companion object {
        const val ACTION_NAME = "testActionName"
        const val TITLE = "Title"
        const val BODY = "TestMessage"
        const val DEFAULT_ACTION_NAME = "testDefaultActionName"
        const val ACTION_ID = "testActionId"
        const val ICON = "https://trunk-int.s.emarsys.com/custloads/218524530/md_100008588.png"
        const val IMAGE = "https://trunk-int.s.emarsys.com/custloads/218524530/md_100008589.png"
        const val ACTION_TITLE = "actionTitle"
        const val BADGE_VALUE = 10
        const val TRACKING_INFO = """{"key":"value"}"""
        const val REPORTING = """{"reportingKey":"reportingValue"}"""
        const val REPORTING2 = """{"reportingKey2":"reportingValue2"}"""
    }

    private lateinit var json: Json
    private lateinit var logger: Logger
    private lateinit var pushMessageWebV2Mapper: PushMessageWebV2Mapper

    @BeforeTest
    fun setUp() = runTest {
        json = JsonUtil.json
        logger = mock(MockMode.autofill)
        pushMessageWebV2Mapper = PushMessageWebV2Mapper(json, logger)
    }


    @Test
    fun map_shouldCreate_jsPushMessage_fromRemotePayload() = runTest {
        val notification = buildJsonObject {
            put("silent", false)
            put("title", TITLE)
            put("body", BODY)
            put("icon", ICON)
            put("imageUrl", IMAGE)
            putJsonObject("defaultAction") {
                put("id", "defaultActionId")
                put("reporting", REPORTING)
                put("title", ACTION_TITLE)
                put("name", DEFAULT_ACTION_NAME)
                put("type", "MECustomEvent")
                putJsonObject("payload") {
                    put("defaultActionPayloadKey", "defaultActionPayloadValue")
                }
            }
            putJsonArray("actions") {
                addJsonObject {
                    put("id", ACTION_ID)
                    put("reporting", REPORTING2)
                    put("title", ACTION_TITLE)
                    put("name", ACTION_NAME)
                    put("type", "MEAppEvent")
                    putJsonObject("payload") {
                        put("actionPayloadKey", "actionPayloadValue")
                    }
                }
            }
            putJsonObject("badgeCount") {
                put("method", "ADD")
                put("value", BADGE_VALUE)
            }
        }

        val ems = buildJsonObject {
            put("version", "WEB_V2")
            put("trackingInfo", buildJsonObject {
                put("key", "value")
            }.toString())
            putJsonObject("rootParams") {
                put("rootKey", "rootValue")
            }
            putJsonObject("customData") {
                put("customKey", "customValue")
            }
        }

        val testMessage = buildJsonObject {
            put("notification", notification)
            put("ems", ems)
        }

        val expectedMessage = JsPushMessage(
            TRACKING_INFO,
            platformData = JsPlatformData,
            badgeCount = BadgeCount(BadgeCountMethod.ADD, BADGE_VALUE),
            actionableData = ActionableData(
                defaultTapAction = BasicCustomEventActionModel(
                    DEFAULT_ACTION_NAME,
                    mapOf("defaultActionPayloadKey" to "defaultActionPayloadValue")
                ),
                actions = listOf(
                    PresentableAppEventActionModel(
                        ACTION_ID,
                        REPORTING2,
                        ACTION_TITLE,
                        ACTION_NAME,
                        mapOf("actionPayloadKey" to "actionPayloadValue")
                    )
                )
            ),
            displayableData = DisplayableData(
                TITLE,
                BODY,
                ICON,
                IMAGE
            )
        )

        val result = pushMessageWebV2Mapper.map(testMessage.toString())

        result shouldBe expectedMessage
    }


    @Test
    fun map_shouldReturnNull_whenRemoteMessage_canNotBeDecoded() = runTest {
        val testMessage = "this cannot be decoded"

        pushMessageWebV2Mapper.map(testMessage) shouldBe null
    }
}