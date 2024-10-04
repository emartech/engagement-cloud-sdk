package com.emarsys.api.push

import com.emarsys.api.generic.ApiContext
import com.emarsys.core.storage.TypedStorageApi
import com.emarsys.networking.clients.push.PushClientApi
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
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
        pushInternal = PushInternal(mockPushClient, mockStorage, pushContext)
    }

    @Test
    fun testRegisterPushToken_shouldNotDoAnything_whenPushTokenStoredAlready() = runTest {
        every { mockStorage.get("emsPushToken") } returns PUSH_TOKEN

        pushInternal.registerPushToken(PUSH_TOKEN)

        verifySuspend {
            repeat(0) {
                mockPushClient.registerPushToken(PUSH_TOKEN)
            }
        }
    }

    @Test
    fun testRegisterPushToken_shouldRegisterPushToken_whenPushTokenWasNotStoredPreviously() =
        runTest {
            every { mockStorage.get("emsPushToken") } returns null
            every { mockStorage.put("emsPushToken", PUSH_TOKEN) } returns Unit
            everySuspend { mockPushClient.registerPushToken(any()) } returns Unit

            pushInternal.registerPushToken(PUSH_TOKEN)

            verifySuspend {
                mockPushClient.registerPushToken(PUSH_TOKEN)
                mockStorage.put("emsPushToken", PUSH_TOKEN)
            }
        }

    @Test
    fun testRegisterPushToken_shouldRegisterPushToken_whenStoredPushTokenIsDifferent() = runTest {
        every { mockStorage.get("emsPushToken") } returns "<differentTestPushToken>"
        every { mockStorage.put("emsPushToken", PUSH_TOKEN) } returns Unit
        everySuspend { mockPushClient.registerPushToken(any()) } returns Unit

        pushInternal.registerPushToken(PUSH_TOKEN)

        verifySuspend {
            mockPushClient.registerPushToken(PUSH_TOKEN)
            mockStorage.put("emsPushToken", PUSH_TOKEN)
        }
    }

    @Test
    fun testClearPushToken() = runTest {
        everySuspend { mockPushClient.clearPushToken() } returns Unit
        everySuspend { mockStorage.put(PushConstants.PUSH_TOKEN_STORAGE_KEY, null) } returns Unit

        pushInternal.clearPushToken()

        verifySuspend {
            mockPushClient.clearPushToken()
        }
    }

    @Test
    fun testPushToken() = runTest {
        every { mockStorage.get(PushConstants.PUSH_TOKEN_STORAGE_KEY) } returns PUSH_TOKEN
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
