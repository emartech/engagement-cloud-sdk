package com.emarsys.api.push

import com.emarsys.api.push.PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY
import com.emarsys.api.push.PushConstants.PUSH_TOKEN_STORAGE_KEY
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.networking.clients.push.PushClientApi
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class PushInternalTests {
    private companion object {
        const val PUSH_TOKEN = "testPushToken"
        val registerPushToken = PushCall.RegisterPushToken(PUSH_TOKEN)
        val clearPushToken = PushCall.ClearPushToken()
        val expectedCalls = mutableListOf(
            registerPushToken,
            clearPushToken
        )
    }

    private lateinit var mockPushClient: PushClientApi
    private lateinit var mockStringStorage: StringStorageApi
    private lateinit var pushContext: PushContextApi
    private lateinit var pushInternal: PushInternal

    @BeforeTest
    fun setup() {
        mockPushClient = mock()
        mockStringStorage = mock()
        pushContext = PushContext(mutableListOf())
        pushInternal = PushInternal(
            mockPushClient,
            mockStringStorage,
            pushContext
        )
    }

    @Test
    fun testRegisterPushToken_shouldNotDoAnything_whenPushTokenStoredAlready() = runTest {
        everySuspend { mockStringStorage.get(PUSH_TOKEN_STORAGE_KEY) } returns PUSH_TOKEN
        everySuspend { mockStringStorage.get(LAST_SENT_PUSH_TOKEN_STORAGE_KEY) } returns PUSH_TOKEN
        everySuspend { mockStringStorage.put(PUSH_TOKEN_STORAGE_KEY, PUSH_TOKEN) } returns Unit
        everySuspend {
            mockStringStorage.put(
                LAST_SENT_PUSH_TOKEN_STORAGE_KEY,
                PUSH_TOKEN
            )
        } returns Unit
        everySuspend { mockPushClient.registerPushToken(any()) } returns Unit

        pushInternal.registerPushToken(PUSH_TOKEN)

        verifySuspend(VerifyMode.exactly(0)) {
            mockPushClient.registerPushToken(PUSH_TOKEN)
        }
    }

    @Test
    fun testRegisterPushToken_shouldRegisterPushToken_whenPushTokenWasNotStoredPreviously() =
        runTest {
            everySuspend { mockStringStorage.get(LAST_SENT_PUSH_TOKEN_STORAGE_KEY) } returns null
            everySuspend { mockStringStorage.put(PUSH_TOKEN_STORAGE_KEY, PUSH_TOKEN) } returns Unit
            everySuspend {
                mockStringStorage.put(
                    LAST_SENT_PUSH_TOKEN_STORAGE_KEY,
                    PUSH_TOKEN
                )
            } returns Unit
            everySuspend { mockPushClient.registerPushToken(any()) } returns Unit

            pushInternal.registerPushToken(PUSH_TOKEN)

            verifySuspend(VerifyMode.order) {
                mockStringStorage.put(PUSH_TOKEN_STORAGE_KEY, PUSH_TOKEN)
                mockPushClient.registerPushToken(PUSH_TOKEN)
                mockStringStorage.put(LAST_SENT_PUSH_TOKEN_STORAGE_KEY, PUSH_TOKEN)
            }
        }

    @Test
    fun testRegisterPushToken_shouldRegisterPushToken_whenStoredPushTokenIsDifferent() = runTest {
        everySuspend { mockStringStorage.get(LAST_SENT_PUSH_TOKEN_STORAGE_KEY) } returns "<differentTestPushToken>"
        everySuspend { mockStringStorage.put(PUSH_TOKEN_STORAGE_KEY, PUSH_TOKEN) } returns Unit
        everySuspend {
            mockStringStorage.put(
                LAST_SENT_PUSH_TOKEN_STORAGE_KEY,
                PUSH_TOKEN
            )
        } returns Unit
        everySuspend { mockPushClient.registerPushToken(any()) } returns Unit

        pushInternal.registerPushToken(PUSH_TOKEN)

        verifySuspend(VerifyMode.order) {
            mockStringStorage.put(PUSH_TOKEN_STORAGE_KEY, PUSH_TOKEN)
            mockPushClient.registerPushToken(PUSH_TOKEN)
            mockStringStorage.put(LAST_SENT_PUSH_TOKEN_STORAGE_KEY, PUSH_TOKEN)
        }
    }

    @Test
    fun testClearPushToken() = runTest {
        everySuspend { mockPushClient.clearPushToken() } returns Unit
        everySuspend { mockStringStorage.put(LAST_SENT_PUSH_TOKEN_STORAGE_KEY, null) } returns Unit

        pushInternal.clearPushToken()

        verifySuspend {
            mockPushClient.clearPushToken()
        }

        verifySuspend { mockStringStorage.put(LAST_SENT_PUSH_TOKEN_STORAGE_KEY, null) }
    }

    @Test
    fun testPushToken() = runTest {
        everySuspend { mockStringStorage.get(PUSH_TOKEN_STORAGE_KEY) } returns PUSH_TOKEN
        everySuspend { mockStringStorage.get(LAST_SENT_PUSH_TOKEN_STORAGE_KEY) } returns null
        everySuspend { mockStringStorage.put(PUSH_TOKEN_STORAGE_KEY, PUSH_TOKEN) } returns Unit
        everySuspend {
            mockStringStorage.put(
                LAST_SENT_PUSH_TOKEN_STORAGE_KEY,
                PUSH_TOKEN
            )
        } returns Unit
        everySuspend { mockPushClient.registerPushToken(any()) } returns Unit

        pushInternal.registerPushToken(PUSH_TOKEN)
        pushInternal.getPushToken() shouldBe PUSH_TOKEN
    }

    @Test
    fun testActivate_should_sendCalls_toPushClient() = runTest {
        everySuspend { mockPushClient.registerPushToken(any()) } returns Unit
        everySuspend { mockPushClient.clearPushToken() } returns Unit

        pushContext.calls.addAll(expectedCalls)

        pushInternal.activate()

        verifySuspend { mockPushClient.registerPushToken(PUSH_TOKEN) }
        verifySuspend { mockPushClient.clearPushToken() }
    }
}
