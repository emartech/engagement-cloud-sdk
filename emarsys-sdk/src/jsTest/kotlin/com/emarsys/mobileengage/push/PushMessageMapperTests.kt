package com.emarsys.mobileengage.push

import com.emarsys.core.log.ConsoleLogger
import com.emarsys.core.log.SdkLogger
import com.emarsys.mobileengage.push.model.JsPlatformData
import com.emarsys.mobileengage.push.model.JsPushMessage
import com.emarsys.util.JsonUtil
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import kotlin.test.BeforeTest
import kotlin.test.Test

class PushMessageMapperTests {
    private companion object {
        const val DEFAULT_CAMPAIGN_ID = "DefaultCampaignId"
        const val TITLE = "Title"
        const val MESSAGE = "TestMessage"
        const val ID = "testId"
        const val SID = "testSid"
        const val APPLICATION_CODE = "testAppCode"
        const val ICON = "https://trunk-int.s.emarsys.com/custloads/218524530/md_100008588.png"
        const val IMAGE = "https://trunk-int.s.emarsys.com/custloads/218524530/md_100008589.png"
    }

    private lateinit var pushMessageMapper: PushMessageMapper
    private lateinit var json: Json
    private lateinit var logger: SdkLogger

    @BeforeTest
    fun setUp() = runTest {
        json = JsonUtil.json
        logger = SdkLogger(ConsoleLogger())
        pushMessageMapper = PushMessageMapper(json, logger)
    }

    @Test
    fun map_shouldCreate_jsPushMessage_fromRemotePayload() = runTest {
        val treatments = buildJsonObject {
            putJsonObject("ui_test") {
                buildJsonObject {
                    put("id", "0")
                    put("run_id", "172130784964773853923")
                }
            }
        }
        val notificationSettings = buildJsonObject {
            put("icon", ICON)
            put("link", "https://sap.com")
            put("image", IMAGE)
        }
        val messageData = buildJsonObject {
            put("id", ID)
            put("sid", SID)
            put("applicationCode", APPLICATION_CODE)
            put("treatments", treatments)
            put("notificationSettings", notificationSettings)
        }

        val testRemoteMessage = buildJsonObject {
            put("title", TITLE)
            put("message", MESSAGE)
            put("messageData", messageData)
        }

        val expectedJsMessage = JsPushMessage(
            ID,
            TITLE,
            MESSAGE,
            ICON,
            IMAGE,
            data = PushData(
                false,
                SID,
                DEFAULT_CAMPAIGN_ID,
                JsPlatformData(
                    APPLICATION_CODE
                )
            )
        )

        val result = pushMessageMapper.map(testRemoteMessage.jsonObject.toString())

        result shouldBe expectedJsMessage
    }

    @Test
    fun map_shouldReturnNull_whenRemoteMessage_canNotBeDecoded() = runTest {
        val testMessage = "this cannot be decoded"

        pushMessageMapper.map(testMessage) shouldBe null
    }
}