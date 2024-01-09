package com.emarsys.api.push

import com.emarsys.api.SdkState
import com.emarsys.context.SdkContextApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PushTests : TestsWithMocks() {
    override fun setUpMocks() = injectMocks(mocker)

    private companion object {
        const val pushToken = "testPushToken"
    }

    @Mock
    lateinit var mockLoggingPush: PushInstance

    @Mock
    lateinit var mockGathererPush: PushInstance

    @Mock
    lateinit var mockPushInternal: PushInstance

    @Mock
    lateinit var mockSdkContext: SdkContextApi

    lateinit var push: Push<PushInstance, PushInstance, PushInstance>

    init {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @BeforeTest
    fun setup() = runTest {
        every { mockSdkContext.sdkDispatcher } returns StandardTestDispatcher()

        everySuspending { mockLoggingPush.activate() } returns Unit
        everySuspending { mockGathererPush.activate() } returns Unit
        everySuspending { mockPushInternal.activate() } returns Unit
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        mocker.reset()
    }

    @Test
    fun test_registerPushToken_inactiveState() = runTest {
        every { mockSdkContext.sdkState } returns MutableStateFlow(SdkState.inactive)
        everySuspending {
            mockLoggingPush.registerPushToken(
                pushToken
            )
        } returns Unit

        push =
            Push(mockLoggingPush, mockGathererPush, mockPushInternal, mockSdkContext)

        push.registerPushToken(pushToken)

        verifyWithSuspend(exhaustive = false) {
            mockLoggingPush.registerPushToken(
                pushToken
            )
        }
    }

    @Test
    fun test_linkContact_onHoldState() = runTest {
        every { mockSdkContext.sdkState } returns MutableStateFlow(SdkState.onHold)
        everySuspending {
            mockGathererPush.registerPushToken(
                pushToken
            )
        } returns Unit

        push =
            Push(mockLoggingPush, mockGathererPush, mockPushInternal, mockSdkContext)

        push.registerPushToken(pushToken)

        verifyWithSuspend(exhaustive = false) {
            mockGathererPush.registerPushToken(
                pushToken
            )
        }
    }

    @Test
    fun test_linkContact_activeState() = runTest {
        every { mockSdkContext.sdkState } returns MutableStateFlow(SdkState.active)
        everySuspending {
            mockPushInternal.registerPushToken(
                pushToken
            )
        } returns Unit

        push =
            Push(mockLoggingPush, mockGathererPush, mockPushInternal, mockSdkContext)

        push.registerPushToken(pushToken)

        verifyWithSuspend(exhaustive = false) {
            mockPushInternal.registerPushToken(
                pushToken
            )
        }
    }

    @Test
    fun test_clearPushToken_inactiveState() = runTest {
        every { mockSdkContext.sdkState } returns MutableStateFlow(SdkState.inactive)
        everySuspending {
            mockLoggingPush.clearPushToken()
        } returns Unit

        push =
            Push(mockLoggingPush, mockGathererPush, mockPushInternal, mockSdkContext)

        push.clearPushToken()

        verifyWithSuspend(exhaustive = false) {
            mockLoggingPush.clearPushToken()
        }
    }

    @Test
    fun test_clearPushToken_onHoldState() = runTest {
        every { mockSdkContext.sdkState } returns MutableStateFlow(SdkState.onHold)
        everySuspending {
            mockGathererPush.clearPushToken()
        } returns Unit

        push =
            Push(mockLoggingPush, mockGathererPush, mockPushInternal, mockSdkContext)

        push.clearPushToken()

        verifyWithSuspend(exhaustive = false) {
            mockGathererPush.clearPushToken()
        }
    }

    @Test
    fun test_clearPushToken_activeState() = runTest {
        every { mockSdkContext.sdkState } returns MutableStateFlow(SdkState.active)
        everySuspending {
            mockPushInternal.clearPushToken()
        } returns Unit

        push =
            Push(mockLoggingPush, mockGathererPush, mockPushInternal, mockSdkContext)

        push.clearPushToken()

        verifyWithSuspend(exhaustive = false) {
            mockPushInternal.clearPushToken()
        }
    }
}