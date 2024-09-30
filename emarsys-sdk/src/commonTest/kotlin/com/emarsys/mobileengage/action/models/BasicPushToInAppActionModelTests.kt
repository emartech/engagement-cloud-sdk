package com.emarsys.mobileengage.action.models

import com.emarsys.mobileengage.inapp.PushToInApp
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class BasicPushToInAppActionModelTests {
    private companion object {
        const val URL = "url"
        const val CAMPAIGN_ID = "campaignId"
    }

    @Test
    fun toInternalPushToInAppActionModel_createsInternalPushToInAppActionModel() {
        val testActionModel = BasicPushToInAppActionModel("testName", PushToInApp(CAMPAIGN_ID, URL))
        val expectation = InternalPushToInappActionModel(CAMPAIGN_ID, URL)

        val result = testActionModel.toInternalPushToInAppActionModel()

        result shouldBe expectation
    }
}