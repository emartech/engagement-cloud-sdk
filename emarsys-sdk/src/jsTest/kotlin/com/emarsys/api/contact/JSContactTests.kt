package com.emarsys.api.contact

import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.assertions.throwables.shouldThrow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class JSContactTests {
    private companion object {
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
        jSContact = JSContact(mockContactApi)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun link_shouldCall_link_onEmarsys() = runTest {
        everySuspend {
            mockContactApi.link(
                CONTACT_FIELD_VALUE
            )
        } returns testSuccessResult

        jSContact.link(CONTACT_FIELD_VALUE)

        verifySuspend { mockContactApi.link(CONTACT_FIELD_VALUE) }
    }

    @Test
    fun link_shouldThrowException_ifLink_failed() = runTest {
        everySuspend {
            mockContactApi.link(
                CONTACT_FIELD_VALUE
            )
        } returns testFailedResult

        shouldThrow<Exception> { jSContact.link(CONTACT_FIELD_VALUE) }
    }

    @Test
    fun linkAuthenticated_shouldCall_linkAuthenticatedContact_onEmarsys() = runTest {
        everySuspend {
            mockContactApi.linkAuthenticated(
                OPEN_ID_TOKEN
            )
        } returns testSuccessResult

        jSContact.linkAuthenticated(OPEN_ID_TOKEN)

        verifySuspend { mockContactApi.linkAuthenticated(OPEN_ID_TOKEN) }
    }

    @Test
    fun linkAuthenticated_shouldThrowException_ifLinkAuthenticatedContact_failed() = runTest {
        everySuspend {
            mockContactApi.linkAuthenticated(
                OPEN_ID_TOKEN
            )
        } returns testFailedResult

        shouldThrow<Exception> {
            jSContact.linkAuthenticated(OPEN_ID_TOKEN)
        }
    }

    @Test
    fun unlink_shouldCall_unlink_onEmarsys() = runTest {
        everySuspend { mockContactApi.unlink() } returns testSuccessResult

        jSContact.unlink()

        verifySuspend { mockContactApi.unlink() }
    }

    @Test
    fun unlink_shouldThrowException_ifUnlink_failed() = runTest {
        everySuspend { mockContactApi.unlink() } returns testFailedResult

        shouldThrow<Exception> { jSContact.unlink() }
    }
}