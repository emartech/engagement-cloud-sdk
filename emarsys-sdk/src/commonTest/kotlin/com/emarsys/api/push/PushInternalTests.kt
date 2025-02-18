package com.emarsys.api.push

import com.emarsys.api.generic.ApiContext
import com.emarsys.api.push.PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY
import com.emarsys.api.push.PushConstants.PUSH_TOKEN_STORAGE_KEY
import com.emarsys.core.storage.TypedStorageApi
import com.emarsys.networking.clients.push.PushClientApi
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
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

        val expected = mutableListOf(
            registerPushToken,
            clearPushToken
        )
    }

    private lateinit var mockPushClient: PushClientApi
    private lateinit var mockStorage: TypedStorageApi<String?>
    private lateinit var pushContext: ApiContext<PushCall>
    private lateinit var pushInternal: PushInternal

    @BeforeTest
    fun setup() = runTest {
        mockPushClient = mock()
        mockStorage = mock()

        pushContext = PushContext(expected)
        pushInternal = PushInternal(mockPushClient, mockStorage, pushContext, mock())
    }

    @Test
    fun testRegisterPushToken_shouldNotDoAnything_whenPushTokenStoredAlready() = runTest {
        every { mockStorage.get(PUSH_TOKEN_STORAGE_KEY) } returns PUSH_TOKEN
        every { mockStorage.get(LAST_SENT_PUSH_TOKEN_STORAGE_KEY) } returns PUSH_TOKEN
        every { mockStorage.put(PUSH_TOKEN_STORAGE_KEY, PUSH_TOKEN) } returns Unit
        every { mockStorage.put(LAST_SENT_PUSH_TOKEN_STORAGE_KEY, PUSH_TOKEN) } returns Unit
        everySuspend { mockPushClient.registerPushToken(any()) } returns Unit

        pushInternal.registerPushToken(PUSH_TOKEN)

        verifySuspend(VerifyMode.exactly(0)) {
            mockPushClient.registerPushToken(PUSH_TOKEN)
        }
    }

    @Test
    fun testRegisterPushToken_shouldRegisterPushToken_whenPushTokenWasNotStoredPreviously() =
        runTest {
            every { mockStorage.get(LAST_SENT_PUSH_TOKEN_STORAGE_KEY) } returns null
            every { mockStorage.put(PUSH_TOKEN_STORAGE_KEY, PUSH_TOKEN) } returns Unit
            every { mockStorage.put(LAST_SENT_PUSH_TOKEN_STORAGE_KEY, PUSH_TOKEN) } returns Unit
            everySuspend { mockPushClient.registerPushToken(any()) } returns Unit

            pushInternal.registerPushToken(PUSH_TOKEN)

            verifySuspend(VerifyMode.order) {
                mockStorage.put(PUSH_TOKEN_STORAGE_KEY, PUSH_TOKEN)
                mockPushClient.registerPushToken(PUSH_TOKEN)
                mockStorage.put(LAST_SENT_PUSH_TOKEN_STORAGE_KEY, PUSH_TOKEN)
            }
        }

    @Test
    fun testRegisterPushToken_shouldRegisterPushToken_whenStoredPushTokenIsDifferent() = runTest {
        every { mockStorage.get(LAST_SENT_PUSH_TOKEN_STORAGE_KEY) } returns "<differentTestPushToken>"
        every { mockStorage.put(PUSH_TOKEN_STORAGE_KEY, PUSH_TOKEN) } returns Unit
        every { mockStorage.put(LAST_SENT_PUSH_TOKEN_STORAGE_KEY, PUSH_TOKEN) } returns Unit
        everySuspend { mockPushClient.registerPushToken(any()) } returns Unit

        pushInternal.registerPushToken(PUSH_TOKEN)

        verifySuspend(VerifyMode.order) {
            mockStorage.put(PUSH_TOKEN_STORAGE_KEY, PUSH_TOKEN)
            mockPushClient.registerPushToken(PUSH_TOKEN)
            mockStorage.put(LAST_SENT_PUSH_TOKEN_STORAGE_KEY, PUSH_TOKEN)
        }
    }

    @Test
    fun testClearPushToken() = runTest {
        everySuspend { mockPushClient.clearPushToken() } returns Unit
        every { mockStorage.put(LAST_SENT_PUSH_TOKEN_STORAGE_KEY, null) } returns Unit

        pushInternal.clearPushToken()

        verifySuspend {
            mockPushClient.clearPushToken()
        }

        verify { mockStorage.put(LAST_SENT_PUSH_TOKEN_STORAGE_KEY, null) }
    }

    @Test
    fun testPushToken() = runTest {
        every { mockStorage.get(PUSH_TOKEN_STORAGE_KEY) } returns PUSH_TOKEN
        every { mockStorage.get(LAST_SENT_PUSH_TOKEN_STORAGE_KEY) } returns null
        every { mockStorage.put(PUSH_TOKEN_STORAGE_KEY, PUSH_TOKEN) } returns Unit
        every { mockStorage.put(LAST_SENT_PUSH_TOKEN_STORAGE_KEY, PUSH_TOKEN) } returns Unit
        everySuspend { mockPushClient.registerPushToken(any()) } returns Unit

        pushInternal.registerPushToken(PUSH_TOKEN)
        pushInternal.pushToken shouldBe PUSH_TOKEN
    }

    @Test
    fun testActivate_should_sendCalls_toPushClient() = runTest {
        everySuspend { mockPushClient.registerPushToken(any()) } returns Unit
        everySuspend { mockPushClient.clearPushToken() } returns Unit

        pushInternal.activate()

        everySuspend { mockPushClient.registerPushToken(PUSH_TOKEN) }
        everySuspend { mockPushClient.clearPushToken() }
    }
}
