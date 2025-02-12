package com.emarsys.mobileengage.push

import com.emarsys.mobileengage.action.models.BasicAppEventActionModel
import com.emarsys.mobileengage.action.models.BasicOpenExternalUrlActionModel
import com.emarsys.util.JsonUtil
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

@ExperimentalCoroutinesApi
class PushUserInfoTests {
    private companion object {
        const val SID = "testSid"
        const val CAMPAIGN_ID = "campaignId"
    }

    @Test
    fun testToBasicPushUserInfo_shouldCreateUserInfoFromValidMap() = runTest {
        val userInfoMap = mapOf(
            "ems" to mapOf(
                "sid" to SID,
                "multichannelId" to CAMPAIGN_ID,
                "actions" to listOf(
                    mapOf(
                        "type" to "OpenExternalUrl",
                        "url" to "https://www.emarsys.com"
                    ),
                    mapOf(
                        "type" to "MEAppEvent",
                        "name" to "name",
                        "payload" to mapOf("key" to "value")
                    )
                )
            )
        )

        val expectedBasicPushUserInfo = BasicPushUserInfo(
            ems = BasicPushUserInfoEms(
                sid = SID,
                multichannelId = CAMPAIGN_ID,
                actions = listOf(
                    BasicOpenExternalUrlActionModel("https://www.emarsys.com"),
                    BasicAppEventActionModel("name", mapOf("key" to "value"))
                )
            )
        )

        userInfoMap.toBasicPushUserInfo(JsonUtil.json) shouldBe expectedBasicPushUserInfo
    }
}