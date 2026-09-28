package com.sap.ec.integration

import com.sap.ec.android.EngagementCloud
import com.sap.ec.api.SdkState
import com.sap.ec.api.config.AndroidEngagementCloudSDKConfig
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.crypto.CryptoApi
import com.sap.ec.core.networking.context.RequestContextApi
import com.sap.ec.di.SdkKoinIsolationContext
import com.sap.ec.event.SdkEvent
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class ContactLinkingIntegrationTests {
    private companion object {
        const val APP_CODE = "EMS-1234"
    }

    private lateinit var sdkContext: SdkContextApi
    private lateinit var requestContext: RequestContextApi
    private lateinit var crypto: CryptoApi
    private lateinit var sdkEventDistributor: SdkEventDistributorApi

    @BeforeTest
    fun setup() = runTest {
        EngagementCloud.initialize()
        requestContext = SdkKoinIsolationContext.koin.get<RequestContextApi>()
        crypto = SdkKoinIsolationContext.koin.get<CryptoApi>()
        sdkEventDistributor = SdkKoinIsolationContext.koin.get<SdkEventDistributorApi>()
        sdkContext = SdkKoinIsolationContext.koin.get<SdkContextApi>()
        sdkContext.setSdkConfig(AndroidEngagementCloudSDKConfig(applicationCode = APP_CODE))
        sdkContext.setSdkState(SdkState.Active)
        requestContext.isContactLinked = false
        requestContext.linkedContactHash = null
    }

    @Test
    fun link_should_not_register_an_event_when_the_same_contact_is_already_linked() = runTest {
        val contactFieldValue = uniqueValue()
        markLinked(contactFieldValue)

        EngagementCloud.contact.link(contactFieldValue)

        linkContactEventCount(contactFieldValue) shouldBe 0
    }

    @Test
    fun link_should_register_an_event_when_a_different_contact_is_linked() = runTest {
        markLinked(uniqueValue())
        val differentContactFieldValue = uniqueValue()

        EngagementCloud.contact.link(differentContactFieldValue)

        linkContactEventCount(differentContactFieldValue) shouldBe 1
    }

    @Test
    fun link_should_register_an_event_when_no_contact_is_linked() = runTest {
        val contactFieldValue = uniqueValue()

        EngagementCloud.contact.link(contactFieldValue)

        linkContactEventCount(contactFieldValue) shouldBe 1
    }

    @Test
    fun linkAuthenticated_should_not_register_an_event_when_the_same_contact_is_already_linked() =
        runTest {
            val openIdToken = uniqueValue()
            markLinked(openIdToken)

            EngagementCloud.contact.linkAuthenticated(openIdToken)

            linkAuthenticatedContactEventCount(openIdToken) shouldBe 0
        }

    private fun uniqueValue(): String = "contact-${Random.nextLong()}"

    private suspend fun markLinked(value: String) {
        requestContext.isContactLinked = true
        requestContext.linkedContactHash = crypto.hash(value)
    }

    private fun linkContactEventCount(contactFieldValue: String): Int =
        sdkEventDistributor.sdkEventFlow.replayCache
            .filterIsInstance<SdkEvent.Internal.Sdk.LinkContact>()
            .count { it.contactFieldValue == contactFieldValue }

    private fun linkAuthenticatedContactEventCount(openIdToken: String): Int =
        sdkEventDistributor.sdkEventFlow.replayCache
            .filterIsInstance<SdkEvent.Internal.Sdk.LinkAuthenticatedContact>()
            .count { it.openIdToken == openIdToken }
}
