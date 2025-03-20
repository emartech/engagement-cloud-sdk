package com.emarsys.setup.states

import com.emarsys.api.push.PushConstants
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.networking.clients.push.PushClientApi
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
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
class RegisterPushTokenStateTests  {
    private companion object {
        const val PUSH_TOKEN = "testPushToken"
        const val LAST_SENT_PUSH_TOKEN = "testLastSentPushToken"
    }

    init {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    private lateinit var mockPushClient: PushClientApi
    private lateinit var mockStringStorage: StringStorageApi
    private lateinit var registerPushTokenState: RegisterPushTokenState

    @BeforeTest
    fun setUp() {
        mockPushClient = mock()
        mockStringStorage = mock()

        registerPushTokenState = RegisterPushTokenState(mockPushClient, mockStringStorage)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testActive_whenLastSentPushTokenIsMissing_pushTokenIsAvailable() = runTest {
        every { mockStringStorage.get(PushConstants.PUSH_TOKEN_STORAGE_KEY) } returns PUSH_TOKEN
        every { mockStringStorage.get(PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY) } returns null
        every { mockStringStorage.put(PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY, PUSH_TOKEN) } returns Unit

        everySuspend { mockPushClient.registerPushToken(PUSH_TOKEN) } returns Unit

        registerPushTokenState.active()

        verifySuspend {
            mockPushClient.registerPushToken(PUSH_TOKEN)
            mockStringStorage.put(
                PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY,
                PUSH_TOKEN
            )
        }
    }

    @Test
    fun testActive_whenBothAvailable_butNotTheSame() = runTest {
        every { mockStringStorage.get(PushConstants.PUSH_TOKEN_STORAGE_KEY) } returns PUSH_TOKEN
        every { mockStringStorage.get(PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY) } returns LAST_SENT_PUSH_TOKEN
        every { mockStringStorage.put(any(), any()) } returns Unit

        everySuspend { mockPushClient.registerPushToken(any()) } returns Unit

        registerPushTokenState.active()

        verifySuspend {
            mockPushClient.registerPushToken(PUSH_TOKEN)
            mockStringStorage.put(
                PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY,
                PUSH_TOKEN
            )
        }
    }

    @Test
    fun testActive_whenBothAvailable_andTheSame() = runTest {
        every { mockStringStorage.get(PushConstants.PUSH_TOKEN_STORAGE_KEY) } returns PUSH_TOKEN
        every { mockStringStorage.get(PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY) } returns LAST_SENT_PUSH_TOKEN
        every { mockStringStorage.put(any(), any()) } returns Unit

        everySuspend { mockPushClient.registerPushToken(any()) } returns Unit

        registerPushTokenState.active()

        verifySuspend {
            repeat(0) {
                mockPushClient.registerPushToken(any())
            }
            repeat(0) {
                mockStringStorage.put(
                    any(),
                    any()
                )
            }
        }
    }

}
