package com.emarsys.mobileengage.push.mappers

import com.emarsys.core.log.ConsoleLogger
import com.emarsys.core.log.SdkLogger
import com.emarsys.mobileengage.action.models.BadgeCount
import com.emarsys.mobileengage.action.models.BadgeCountMethod
import com.emarsys.mobileengage.action.models.BasicOpenExternalUrlActionModel
import com.emarsys.mobileengage.action.models.PresentableOpenExternalUrlActionModel
import com.emarsys.mobileengage.inapp.PushToInApp
import com.emarsys.mobileengage.push.PushData
import com.emarsys.mobileengage.push.model.JsPlatformData
import com.emarsys.mobileengage.push.model.JsPushMessage
import com.emarsys.util.JsonUtil
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
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
        const val URL = "https://www.sap.com"
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
            put("actions", buildJsonArray {
                addJsonObject {
                    put("type", "openExternalUrl")
                    put("id", "actionId1")
                    put("title", "actionTitle1")
                    put("url", "https://www.sap.com")
                }
            })
            put("badgeCount", buildJsonObject {
                put("method", "SET")
                put("value", 8)
            })
        }

        val inAppJson = buildJsonObject {
            put("campaign_id", DEFAULT_CAMPAIGN_ID)
            put("url", URL)
            put("ignoreViewedEvent", true)
        }

        val messageData = buildJsonObject {
            put("id", ID)
            put("sid", SID)
            put("applicationCode", APPLICATION_CODE)
            put("treatments", treatments)
            put("notificationSettings", notificationSettings)
            put("inApp", inAppJson)
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
                ),
                pushToInApp = PushToInApp(
                    DEFAULT_CAMPAIGN_ID,
                    URL,
                    true
                ),
                actions = listOf(
                    PresentableOpenExternalUrlActionModel(
                        id = "actionId1",
                        title = "actionTitle1",
                        url = "https://www.sap.com"
                    ),
                ),
                defaultTapAction = BasicOpenExternalUrlActionModel("https://sap.com"),
                badgeCount = BadgeCount(BadgeCountMethod.SET, 8)
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