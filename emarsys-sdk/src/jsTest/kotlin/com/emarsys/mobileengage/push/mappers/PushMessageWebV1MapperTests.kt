package com.emarsys.mobileengage.push.mappers

import com.emarsys.core.log.ConsoleLogger
import com.emarsys.core.log.SdkLogger
import com.emarsys.mobileengage.action.models.BadgeCount
import com.emarsys.mobileengage.action.models.BadgeCountMethod
import com.emarsys.mobileengage.action.models.BasicCustomEventActionModel
import com.emarsys.mobileengage.action.models.PresentableAppEventActionModel
import com.emarsys.mobileengage.push.PresentablePushData
import com.emarsys.mobileengage.push.model.JsPlatformData
import com.emarsys.mobileengage.push.model.JsPushMessage
import com.emarsys.util.JsonUtil
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


class PushMessageWebV1MapperTests {
    private companion object {
        const val ACTION_NAME = "testActionName"
        const val TITLE = "Title"
        const val BODY = "TestMessage"
        const val ID = "testId"
        const val SID = "testSid"
        const val APPLICATION_CODE = "testAppCode"
        const val CAMPAIGN_ID = "testCampaignId"
        const val PRODUCT_ID = "testProductId"
        const val DEFAULT_ACTION_NAME = "testDefaultActionName"
        const val ACTION_ID = "testActionId"
        const val ICON = "https://trunk-int.s.emarsys.com/custloads/218524530/md_100008588.png"
        const val IMAGE = "https://trunk-int.s.emarsys.com/custloads/218524530/md_100008589.png"
        const val ACTION_TITLE = "actionTitle"
        const val BADGE_VALUE = 10
    }

    private lateinit var json: Json
    private lateinit var logger: SdkLogger
    private lateinit var pushMessageWebV1Mapper: PushMessageWebV1Mapper

    @BeforeTest
    fun setUp() = runTest {
        json = JsonUtil.json
        logger = SdkLogger(ConsoleLogger())
        pushMessageWebV1Mapper = PushMessageWebV1Mapper(json, logger)
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
            put("version", "WEB_V1")
            put("id", ID)
            put("applicationCode", APPLICATION_CODE)
            put("campaignId", CAMPAIGN_ID)
            put("productId", PRODUCT_ID)
            put("multiChannelId", "testMultiChannelId")
            put("sid", SID)
            putJsonObject("treatments") {
                put("key", "value")
            }
            putJsonObject("rootParams") {
                put("rootKey", "rootValue")
            }
        }

        val testMessage = buildJsonObject {
            put("notification", notification)
            put("ems", ems)
        }

        val expectedMessage = JsPushMessage(
            ID,
            TITLE,
            BODY,
            ICON,
            IMAGE,
            PresentablePushData(
                false,
                SID,
                CAMPAIGN_ID,
                JsPlatformData(APPLICATION_CODE),
                BasicCustomEventActionModel(
                    DEFAULT_ACTION_NAME,
                    mapOf("defaultActionPayloadKey" to "defaultActionPayloadValue")
                ),
                listOf(
                    PresentableAppEventActionModel(
                        ACTION_ID,
                        ACTION_TITLE,
                        ACTION_NAME,
                        mapOf("actionPayloadKey" to "actionPayloadValue")
                    )
                ),
                badgeCount = BadgeCount(BadgeCountMethod.ADD, BADGE_VALUE)
            )
        )

        val result = pushMessageWebV1Mapper.map(testMessage.toString())

        result shouldBe expectedMessage
    }


    @Test
    fun map_shouldReturnNull_whenRemoteMessage_canNotBeDecoded() = runTest {
        val testMessage = "this cannot be decoded"

        pushMessageWebV1Mapper.map(testMessage) shouldBe null
    }
}