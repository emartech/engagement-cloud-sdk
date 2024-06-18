package com.emarsys.service

import io.kotest.matchers.shouldBe
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Test

class HmsRemoteMessageMapperTest {

    @Test
    fun map_shouldAdd_commonFields_fromMap() {
        val testMap = mapOf(
            "title" to "testTitle",
            "message_id" to "testMessageId",
            "body" to "testBody",
            "icon_url" to "testIconUrlString",
            "image_url" to "testImageUrlString"
        )

        val result = HmsRemoteMessageMapper.map(testMap)

        result.get("title") shouldBe "testTitle"
        result.get("messageId") shouldBe "testMessageId"
        result.get("body") shouldBe "testBody"
        result.get("iconUrlString") shouldBe "testIconUrlString"
        result.get("imageUrlString") shouldBe "testImageUrlString"
    }

    @Test
    fun map_shouldAdd_sid_fromMap() {
        val uPayload = """{"sid":"testSid"}"""

        val testRemoteMessageContent = mapOf(
            "u" to uPayload,
        )

        val result = HmsRemoteMessageMapper.map(testRemoteMessageContent)

        val resultData: JSONObject = result.get("data") as JSONObject
        resultData.get("sid") shouldBe "testSid"
    }

    @Test
    fun map_shouldAdd_u_fromMap() {
        val uPayload = """{"sid":"testSid"}"""
        val testRemoteMessageContent = mapOf(
            "u" to uPayload
        )

        val result = HmsRemoteMessageMapper.map(testRemoteMessageContent)
        val resultData: JSONObject = result.get("data") as JSONObject

        resultData.get("u") shouldBe """{"sid":"testSid"}"""
    }

    @Test
    fun map_shouldAdd_silent_fromMap() {
        val emsPayload = """{"silent":"false"}"""

        val testRemoteMessageContent = mapOf(
            "ems" to emsPayload,
        )

        val result = HmsRemoteMessageMapper.map(testRemoteMessageContent)
        val resultData: JSONObject = result.get("data") as JSONObject

        resultData.get("silent") shouldBe false
    }

    @Test
    fun map_shouldAdd_campaignId_fromMap() {
        val emsPayload = """{"multichannelId":"testCampaignId"}"""

        val testRemoteMessageContent = mapOf(
            "ems" to emsPayload,
        )

        val result = HmsRemoteMessageMapper.map(testRemoteMessageContent)
        val resultData: JSONObject = result.get("data") as JSONObject

        resultData.get("campaignId") shouldBe "testCampaignId"
    }

    @Test
    fun map_shouldAdd_defaultAction_fromMap() {
        val emsPayload = """{"default_action":{"type":"MECustomEvent","name":"testName"}}"""

        val testRemoteMessageContent = mapOf(
            "ems" to emsPayload,
        )

        val result = HmsRemoteMessageMapper.map(testRemoteMessageContent)
        val resultData: JSONObject = result.get("data") as JSONObject

        resultData.get("defaultAction") shouldBe """{"type":"MECustomEvent","name":"testName"}"""
    }

    @Test
    fun map_shouldOmit_defaultAction_fromMap() {
        val emsPayload = """{}"""

        val testRemoteMessageContent = mapOf(
            "ems" to emsPayload,
        )

        val result = HmsRemoteMessageMapper.map(testRemoteMessageContent)
        val resultData: JSONObject = result.get("data") as JSONObject

        resultData.keys().forEach { (it == "defaultAction") shouldBe false }
    }

    @Test
    fun map_shouldAdd_actions_fromMap() {
        val emsPayload = """{"actions":[
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
            ]
            }""".trimIndent()

        val testRemoteMessageContent = mapOf(
            "ems" to emsPayload,
        )

        val result = HmsRemoteMessageMapper.map(testRemoteMessageContent)
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
        val emsPayload = """{}"""

        val testRemoteMessageContent = mapOf(
            "ems" to emsPayload,
        )

        val result = HmsRemoteMessageMapper.map(testRemoteMessageContent)
        val resultData: JSONObject = result.get("data") as JSONObject

        resultData.keys().forEach { (it == "actions") shouldBe false }
    }

    @Test
    fun map_shouldAdd_inapp_fromMap() {
        val emsPayload = """{"inapp":{"campaign_id":"testCampaignId","url":"https:\/\/emarsys.hu"}}"""

        val testRemoteMessageContent = mapOf(
            "ems" to emsPayload,
        )

        val result = HmsRemoteMessageMapper.map(testRemoteMessageContent)
        val resultData: JSONObject = result.get("data") as JSONObject

        resultData.get("inapp") shouldBe """{"campaign_id":"testCampaignId","url":"https:\/\/emarsys.hu"}"""
    }

    @Test
    fun map_shouldOmit_inapp_fromMap() {
        val emsPayload = """{}"""

        val testRemoteMessageContent = mapOf(
            "ems" to emsPayload,
        )

        val result = HmsRemoteMessageMapper.map(testRemoteMessageContent)
        val resultData: JSONObject = result.get("data") as JSONObject

        resultData.keys().forEach { (it == "inapp") shouldBe false }
    }

    @Test
    fun map_shouldAdd_rootParams_fromMap() {
        val testRemoteMessageContent = mapOf(
            "rootParamsKey" to "rootParamsValue"
        )

        val result = HmsRemoteMessageMapper.map(testRemoteMessageContent)
        val resultData: JSONObject = result.get("data") as JSONObject

        resultData.get("rootParams") shouldBe mapOf("rootParamsKey" to "rootParamsValue")
    }

    @Test
    fun map_shouldAdd_platformContext_fromMap() {
        val emsPayload = """{
            "style":"testStyle",
            "notificationMethod":{"collapseId":"12345"}
            }""".trimMargin()
        val testRemoteMessageContent = mapOf(
            "channel_id" to "testChannelId",
            "ems" to emsPayload
        )

        val result = HmsRemoteMessageMapper.map(testRemoteMessageContent)
        val resultData: JSONObject = result.get("data") as JSONObject
        val context: JSONObject = resultData.get("platformContext") as JSONObject
        context.get("style") shouldBe "testStyle"
        context.get("channelId") shouldBe "testChannelId"
        context.get("notificationMethod") shouldBe """{"collapseId":"12345"}"""
    }

    @Test
    fun map_shouldOmit_missingKey_style_fromMap() {
        val emsPayload = """{
            "notificationMethod":{"collapseId":"12345"}
            }""".trimMargin()
        val testRemoteMessageContent = mapOf(
            "channel_id" to "testChannelId",
            "ems" to emsPayload
        )

        val result = HmsRemoteMessageMapper.map(testRemoteMessageContent)
        val resultData: JSONObject = result.get("data") as JSONObject
        val context: JSONObject = resultData.get("platformContext") as JSONObject

        context.keys().forEach { (it == "style") shouldBe false }
    }

    @Test
    fun map_shouldOmit_missingKey_notificationMethod_fromMap() {
        val emsPayload = """{
            "style":"testStyle"
            }""".trimMargin()
        val testRemoteMessageContent = mapOf(
            "channel_id" to "testChannelId",
            "ems" to emsPayload
        )

        val result = HmsRemoteMessageMapper.map(testRemoteMessageContent)
        val resultData: JSONObject = result.get("data") as JSONObject
        val context: JSONObject = resultData.get("platformContext") as JSONObject

        context.keys().forEach { (it == "notificationMethod") shouldBe false }
    }
}