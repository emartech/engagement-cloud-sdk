package com.emarsys.api.contact

import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.assertions.throwables.shouldThrow
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
        val testException = Exception("testException")
        val testFailedResult = Result.failure<Unit>(testException)
        val testSuccessResult = Result.success<Unit>(Unit)
    }

    private lateinit var jSContact: JSContactApi
    private lateinit var mockContactApi: ContactApi

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        mockContactApi = mock()
        jSContact = JSContact(mockContactApi, TestScope())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun link_shouldCall_link_onEmarsys() = runTest {
        everySuspend {
            mockContactApi.link(
                CONTACT_FIELD_ID,
                CONTACT_FIELD_VALUE
            )
        } returns testSuccessResult

        jSContact.link(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE).await()

        verifySuspend { mockContactApi.link(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE) }
    }

    @Test
    fun link_shouldThrowException_ifLink_failed() = runTest {
        everySuspend {
            mockContactApi.link(
                CONTACT_FIELD_ID,
                CONTACT_FIELD_VALUE
            )
        } returns testFailedResult

        shouldThrow<Exception> { jSContact.link(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE).await() }
    }

    @Test
    fun linkAuthenticated_shouldCall_linkAuthenticatedContact_onEmarsys() = runTest {
        everySuspend {
            mockContactApi.linkAuthenticated(
                CONTACT_FIELD_ID,
                OPEN_ID_TOKEN
            )
        } returns testSuccessResult

        jSContact.linkAuthenticated(CONTACT_FIELD_ID, OPEN_ID_TOKEN).await()

        verifySuspend { mockContactApi.linkAuthenticated(CONTACT_FIELD_ID, OPEN_ID_TOKEN) }
    }

    @Test
    fun linkAuthenticated_shouldThrowException_ifLinkAuthenticatedContact_failed() = runTest {
        everySuspend {
            mockContactApi.linkAuthenticated(
                CONTACT_FIELD_ID,
                OPEN_ID_TOKEN
            )
        } returns testFailedResult

        shouldThrow<Exception> {
            jSContact.linkAuthenticated(CONTACT_FIELD_ID, OPEN_ID_TOKEN).await()
        }
    }

    @Test
    fun unlink_shouldCall_unlink_onEmarsys() = runTest {
        everySuspend { mockContactApi.unlink() } returns testSuccessResult

        jSContact.unlink().await()

        verifySuspend { mockContactApi.unlink() }
    }

    @Test
    fun unlink_shouldThrowException_ifUnlink_failed() = runTest {
        everySuspend { mockContactApi.unlink() } returns testFailedResult

        shouldThrow<Exception> { jSContact.unlink().await() }
    }
}