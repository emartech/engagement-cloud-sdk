package com.emarsys.service

import io.kotest.matchers.equality.shouldBeEqualUsingFields
import io.kotest.matchers.shouldBe
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Test
import java.util.UUID

class FirebaseRemoteMessageMapperTest {

    @Test
    fun map_shouldAdd_commonFields_fromMap() {
        val testMap = mapOf(
            "notification.title" to "testTitle",
            "ems.message_id" to "testMessageId",
            "notification.body" to "testBody",
            "notification.icon" to "testIconUrlString",
            "notification.image" to "testImageUrlString"
        )

        val result = FirebaseRemoteMessageMapper.map(testMap)

        result.get("title") shouldBe "testTitle"
        result.get("messageId") shouldBe "testMessageId"
        result.get("body") shouldBe "testBody"
        result.get("iconUrlString") shouldBe "testIconUrlString"
        result.get("imageUrlString") shouldBe "testImageUrlString"
    }

    @Test
    fun map_shouldAdd_sid_fromMap() {
        val testRemoteMessageContent = mapOf(
            "ems.sid" to "testSid"
        )

        val result = FirebaseRemoteMessageMapper.map(testRemoteMessageContent)

        val resultData: JSONObject = result.get("data") as JSONObject
        resultData.get("sid") shouldBe "testSid"
    }

    @Test
    fun map_shouldAdd_u_fromMap() {
        val uPayload = """{"testKey":"testValue"}"""
        val rootParams = JSONObject().put("u", uPayload).toString()

        val testRemoteMessageContent = mapOf(
            "ems.root_params" to rootParams,
        )

        val result = FirebaseRemoteMessageMapper.map(testRemoteMessageContent)
        val resultData: JSONObject = result.get("data") as JSONObject

        resultData.get("u") shouldBe """{"testKey":"testValue"}"""
    }

    @Test
    fun map_shouldAdd_silent_fromMap() {
        val testRemoteMessageContent = mapOf(
            "ems.silent" to "true",
        )

        val result = FirebaseRemoteMessageMapper.map(testRemoteMessageContent)
        val resultData: JSONObject = result.get("data") as JSONObject

        resultData.get("silent") shouldBe "true"
    }

    @Test
    fun map_shouldAdd_campaignId_fromMap() {
        val testRemoteMessageContent = mapOf(
            "ems.multichannel_id" to "testCampaignId"
        )

        val result = FirebaseRemoteMessageMapper.map(testRemoteMessageContent)
        val resultData: JSONObject = result.get("data") as JSONObject

        resultData.get("campaignId") shouldBe "testCampaignId"
    }

    @Test
    fun map_shouldAdd_defaultAction_fromMap() {
        val testRemoteMessageContent = mapOf(
            "ems.tap_actions.default_action.name" to "testName",
            "ems.tap_actions.default_action.type" to "MECustomEvent",
            "ems.tap_actions.default_action.payload" to """{"key":"value"}""",
        )

        val result = FirebaseRemoteMessageMapper.map(testRemoteMessageContent)
        val resultData: JSONObject = result.get("data") as JSONObject

        resultData.get("defaultAction") shouldBeEqualUsingFields JSONObject("""{"type":"MECustomEvent","name":"testName","payload":{"key":"value"}}""")
    }

    @Test
    fun map_shouldOmit_defaultAction_fromMap_whenTypeIsMissing() {
        val testRemoteMessageContent = mapOf(
            "ems.tap_actions.default_action.name" to "testName",
            "ems.tap_actions.default_action.payload" to """{"key":"value"}""",
        )

        val result = FirebaseRemoteMessageMapper.map(testRemoteMessageContent)
        val resultData: JSONObject = result.get("data") as JSONObject

        resultData.keys().forEach { (it == "defaultAction") shouldBe false }
    }

    @Test
    fun map_shouldAdd_actions_fromMap() {
        val actions = JSONArray("""[
         {
            "payload":{
               "key":"value"
            },
            "name":"mysy3",
            "id":"custom",
            "type":"MECustomEvent",
            "title":"customEvent"
         },
         {
            "name":"Test",
            "index":2,
            "id":"External url test",
            "type":"OpenExternalUrl",
            "title":"ExternalURL",
            "url":"https:\/\/www.emarsys.com"
         }
         ]""".trimIndent()).toString()

        val testRemoteMessageContent = mapOf(
            "ems.actions" to actions,
        )

        val result = FirebaseRemoteMessageMapper.map(testRemoteMessageContent)
        val resultData: JSONObject = result.get("data") as JSONObject

        resultData.get("actions") shouldBe JSONArray("""[
         {
            "payload":{
               "key":"value"
            },
            "name":"mysy3",
            "id":"custom",
            "type":"MECustomEvent",
            "title":"customEvent"
         },
         {"name":"Test",
            "index":2,
            "id":"External url test",
            "type":"OpenExternalUrl",
            "title":"ExternalURL",
            "url":"https:\/\/www.emarsys.com"
         }
            ]""").toString()
    }

    @Test
    fun map_shouldOmit_actions_fromMap() {
        val testRemoteMessageContent = emptyMap<String, String>()

        val result = FirebaseRemoteMessageMapper.map(testRemoteMessageContent)
        val resultData: JSONObject = result.get("data") as JSONObject

        resultData.keys().forEach { (it == "actions") shouldBe false }
    }

    @Test
    fun map_shouldAdd_inapp_fromMap() {
        val testRemoteMessageContent = mapOf(
            "ems.inapp" to """{"campaign_id":"testCampaignId","url":"https:\/\/emarsys.hu"}""",
        )

        val result = FirebaseRemoteMessageMapper.map(testRemoteMessageContent)
        val resultData: JSONObject = result.get("data") as JSONObject

        resultData.get("inapp") shouldBe """{"campaign_id":"testCampaignId","url":"https:\/\/emarsys.hu"}"""
    }

    @Test
    fun map_shouldOmit_inapp_fromMap() {
        val testRemoteMessageContent = emptyMap<String, String>()

        val result = FirebaseRemoteMessageMapper.map(testRemoteMessageContent)
        val resultData: JSONObject = result.get("data") as JSONObject

        resultData.keys().forEach { (it == "inapp") shouldBe false }
    }

    @Test
    fun map_shouldAdd_rootParams_fromMap() {
        val testRemoteMessageContent = mapOf(
            "ems.root_params" to """{"rootParamsKey":"rootParamsValue"}"""
        )

        val result = FirebaseRemoteMessageMapper.map(testRemoteMessageContent)
        val resultData: JSONObject = result.get("data") as JSONObject

        resultData.get("rootParams") shouldBeEqualUsingFields JSONObject("""{"rootParamsKey":"rootParamsValue"}""")
    }

    @Test
    fun map_shouldAdd_platformContext_fromMap() {
        val testRemoteMessageContent = mapOf(
            "notification.channel_id" to "testChannelId",
            "ems.style" to "testStyle",
            "ems.notification_method.collapse_key" to "testCollapseKey",
            "ems.notification_method.operation" to "DELETE",
        )

        val result = FirebaseRemoteMessageMapper.map(testRemoteMessageContent)
        val resultData: JSONObject = result.get("data") as JSONObject
        val context: JSONObject = resultData.get("platformContext") as JSONObject
        context.get("style") shouldBe "testStyle"
        context.get("channelId") shouldBe "testChannelId"
        context.get("notificationMethod") shouldBeEqualUsingFields JSONObject("""{"collapseId":"testCollapseKey","operation":"DELETE"}""")
    }

    @Test
    fun map_shouldOmit_missingKey_style_fromMap() {
        val testRemoteMessageContent = mapOf(
            "notification.channel_id" to "testChannelId"
        )

        val result = FirebaseRemoteMessageMapper.map(testRemoteMessageContent)
        val resultData: JSONObject = result.get("data") as JSONObject
        val context: JSONObject = resultData.get("platformContext") as JSONObject

        context.keys().forEach { (it == "style") shouldBe false }
    }

    @Test
    fun map_shouldAddDefault_notificationMethod_fromMap() {
        val testRemoteMessageContent = mapOf(
            "ems.style" to "testStyle",
        )

        val result = FirebaseRemoteMessageMapper.map(testRemoteMessageContent)
        val resultData: JSONObject = result.get("data") as JSONObject
        val context: JSONObject = resultData.get("platformContext") as JSONObject
        val method = context.get("notificationMethod") as JSONObject

        method["operation"] shouldBe "INIT"
        UUID.fromString(method["collapseId"] as String)
    }
}