package com.emarsys.setup.states

import com.emarsys.api.push.PushConstants
import com.emarsys.core.storage.StorageApi
import com.emarsys.networking.clients.push.PushClientApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.AfterTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterPushTokenStateTests : TestsWithMocks() {
    override fun setUpMocks() = injectMocks(mocker)

    @Mock
    lateinit var mockPushClient: PushClientApi

    @Mock
    lateinit var mockStorage: StorageApi<String>

    private val registerPushTokenState: RegisterPushTokenState by withMocks {
        RegisterPushTokenState(mockPushClient, mockStorage)
    }

    private companion object {
        const val PUSH_TOKEN = "testPushToken"
        const val LAST_SENT_PUSH_TOKEN = "testLastSentPushToken"

    }

    init {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        mocker.reset()
        Dispatchers.resetMain()

    }

    @Test
    fun testActive_whenLastSentPushTokenIsMissing_pushTokenIsAvailable() = runTest {
        every { mockStorage.get(PushConstants.PUSH_TOKEN_STORAGE_KEY) } returns PUSH_TOKEN
        every { mockStorage.get(PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY) } returns null
        every { mockStorage.put(PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY, PUSH_TOKEN) } returns Unit

        everySuspending { mockPushClient.registerPushToken(PUSH_TOKEN) } returns Unit

        registerPushTokenState.active()

        verifyWithSuspend(exhaustive = false) {
            mockPushClient.registerPushToken(PUSH_TOKEN)
            mockStorage.put(
                PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY,
                PUSH_TOKEN
            )
        }
    }

    @Test
    fun testActive_whenBothAvailable_butNotTheSame() = runTest {
        every { mockStorage.get(PushConstants.PUSH_TOKEN_STORAGE_KEY) } returns PUSH_TOKEN
        every { mockStorage.get(PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY) } returns LAST_SENT_PUSH_TOKEN
        every { mockStorage.put(isAny(), isAny()) } returns Unit

        everySuspending { mockPushClient.registerPushToken(isAny()) } returns Unit

        registerPushTokenState.active()

        verifyWithSuspend(exhaustive = false) {
            mockPushClient.registerPushToken(PUSH_TOKEN)
            mockStorage.put(
                PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY,
                PUSH_TOKEN
            )
        }
    }

    @Test
    fun testActive_whenBothAvailable_andTheSame() = runTest {
        every { mockStorage.get(PushConstants.PUSH_TOKEN_STORAGE_KEY) } returns PUSH_TOKEN
        every { mockStorage.get(PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY) } returns LAST_SENT_PUSH_TOKEN
        every { mockStorage.put(isAny(), isAny()) } returns Unit

        everySuspending { mockPushClient.registerPushToken(isAny()) } returns Unit

        registerPushTokenState.active()

        verifyWithSuspend(exhaustive = false) {
            repeat(0) {
                mockPushClient.registerPushToken(isAny())
            }
            repeat(0) {
                mockStorage.put(
                    isAny(),
                    isAny()
                )
            }
        }
    }

}
