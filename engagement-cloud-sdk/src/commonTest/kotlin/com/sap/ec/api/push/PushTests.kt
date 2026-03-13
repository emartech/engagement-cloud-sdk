package com.sap.ec.api.push

import com.sap.ec.api.SdkState
import com.sap.ec.context.SdkContextApi
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PushTests {
    private companion object {
        const val PUSH_TOKEN = "testPushToken"
    }

    private lateinit var mockLoggingPush: PushInstance
    private lateinit var mockGathererPush: PushInstance
    private lateinit var mockPushInternal: PushInstance
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var push: Push<PushInstance, PushInstance, PushInstance>

    @BeforeTest
    fun setup() {
        val mainDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(mainDispatcher)

        mockLoggingPush = mock(MockMode.autofill)
        mockGathererPush = mock(MockMode.autofill)
        mockPushInternal = mock(MockMode.autofill)
        mockSdkContext = mock(MockMode.autofill)
        every { mockSdkContext.sdkDispatcher } returns mainDispatcher

        push = Push(
            mockLoggingPush,
            mockGathererPush,
            mockPushInternal,
            mockSdkContext
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testRegisterToken_inactiveState() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Initialized)
        everySuspend {
            mockLoggingPush.registerPushToken(PUSH_TOKEN)
        } returns Unit
        push.registerOnContext()

        push.registerToken(PUSH_TOKEN)

        verifySuspend { mockLoggingPush.registerPushToken(PUSH_TOKEN) }
    }

    @Test
    fun testRegisterToken_onHoldState() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.OnHold)
        push.registerOnContext()

        push.registerToken(PUSH_TOKEN)

        verifySuspend { mockGathererPush.registerPushToken(PUSH_TOKEN) }
    }

    @Test
    fun testRegisterToken_activeState() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Active)
        push.registerOnContext()

        push.registerToken(PUSH_TOKEN)

        verifySuspend { mockPushInternal.registerPushToken(PUSH_TOKEN) }
    }

    @Test
    fun testRegisterToken_activeState_when_throws() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Active)
        val expectedException = Exception()
        everySuspend { mockPushInternal.registerPushToken(PUSH_TOKEN) } throws expectedException
        push.registerOnContext()

        val result = push.registerToken(PUSH_TOKEN)

        result.onFailure { it shouldBe expectedException }
    }

    @Test
    fun testClearToken_inactiveState() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Initialized)
        push.registerOnContext()

        push.clearToken()

        verifySuspend { mockLoggingPush.clearPushToken() }
    }

    @Test
    fun testClearToken_onHoldState() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.OnHold)
        push.registerOnContext()

        push.clearToken()

        verifySuspend { mockGathererPush.clearPushToken() }
    }

    @Test
    fun testClearToken_activeState() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Active)
        push.registerOnContext()

        push.clearToken()

        verifySuspend { mockPushInternal.clearPushToken() }
    }

    @Test
    fun testClearToken_activeState_when_throws() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Active)
        val expectedException = Exception()
        everySuspend { mockPushInternal.clearPushToken() } throws expectedException
        push.registerOnContext()

        val result = push.clearToken()

        result.onFailure { it shouldBe expectedException }
    }

    @Test
    fun testPushToken_inactiveState() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Initialized)
        everySuspend { mockLoggingPush.getPushToken() } returns null
        push.registerOnContext()

        val result = push.getToken()

        result.onSuccess { it shouldBe null }
    }

    @Test
    fun testPushToken_onHoldState() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.OnHold)
        everySuspend { mockGathererPush.getPushToken() } returns PUSH_TOKEN
        push.registerOnContext()

        val result = push.getToken()

        result.onSuccess { it shouldBe PUSH_TOKEN }
    }

    @Test
    fun testPushToken_activeState() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Active)
        everySuspend { mockPushInternal.getPushToken() } returns PUSH_TOKEN

        push.registerOnContext()

        val result = push.getToken()

        result.onSuccess { it shouldBe PUSH_TOKEN }
    }

    @Test
    fun registerToken_shouldPropagateCancellationException() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Active)
        everySuspend { mockPushInternal.registerPushToken(PUSH_TOKEN) } throws CancellationException("test cancellation")
        push.registerOnContext()

        shouldThrow<CancellationException> { push.registerToken(PUSH_TOKEN) }
    }
}
