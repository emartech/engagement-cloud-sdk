package com.emarsys.api.contact

import dev.mokkery.MockMode
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.await
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class JSContactTests {
    private companion object {
        const val CONTACT_FIELD_ID = 2334
        const val CONTACT_FIELD_VALUE = "testContactFieldValue"
        const val OPEN_ID_TOKEN = "testOpenIdToken"
    }

    private lateinit var jSContact: JSContactApi
    private lateinit var mockContactApi: ContactApi

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        mockContactApi = mock(MockMode.autofill)
        jSContact = JSContact(mockContactApi, TestScope())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun link_shouldCall_linkContact_onEmarsys() = runTest {
        jSContact.link(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE).await()

        verifySuspend { mockContactApi.linkContact(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE) }
    }

    @Test
    fun linkAuthenticated_shouldCall_linkAuthenticatedContact_onEmarsys() = runTest {
        jSContact.linkAuthenticated(CONTACT_FIELD_ID, OPEN_ID_TOKEN).await()

        verifySuspend { mockContactApi.linkAuthenticatedContact(CONTACT_FIELD_ID, OPEN_ID_TOKEN) }
    }

    @Test
    fun unlink_shouldCall_unlinkContact_onEmarsys() = runTest {
        jSContact.unlink().await()

        verifySuspend { mockContactApi.unlinkContact() }
    }
}