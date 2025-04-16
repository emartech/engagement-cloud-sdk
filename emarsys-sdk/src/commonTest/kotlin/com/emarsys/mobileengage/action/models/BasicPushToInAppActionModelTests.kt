package com.emarsys.mobileengage.action.models

import com.emarsys.mobileengage.inapp.PushToInAppPayload
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class BasicPushToInAppActionModelTests {
    private companion object {
        const val URL = "url"
        const val CAMPAIGN_ID = "campaignId"
        const val ID = "testId"
        const val REPORTING = """{"reportingKey":"reportingValue"}"""
    }

    @Test
    fun toPresentablePushToInAppActionModel_createsPresentablePushToInAppActionModel() {
        val expectedActionModel = PresentablePushToInAppActionModel(
            ID,
            REPORTING,
            "",
            PushToInAppPayload(CAMPAIGN_ID, URL)
        )
        val testActionModel =
            BasicPushToInAppActionModel(ID, REPORTING, PushToInAppPayload(CAMPAIGN_ID, URL))

        val result = testActionModel.toPresentablePushToInAppActionModel()

        result shouldBe expectedActionModel
    }
}