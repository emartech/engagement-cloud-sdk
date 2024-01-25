package com.emarsys.api.push

import com.emarsys.api.SdkResult
import com.emarsys.api.SdkState
import com.emarsys.context.DefaultUrls
import com.emarsys.context.SdkContext
import com.emarsys.context.SdkContextApi
import com.emarsys.core.log.LogLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    private lateinit var sdkContext: SdkContextApi

    lateinit var push: Push<PushInstance, PushInstance, PushInstance>

    init {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @BeforeTest
    fun setup() = runTest {
        sdkContext = SdkContext(
            StandardTestDispatcher(),
            DefaultUrls("", "", "", "", "", "", ""),
            LogLevel.error,
            mutableSetOf()
        )

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
        everySuspending {
            mockLoggingPush.registerPushToken(
                pushToken
            )
        } returns SdkResult.Success(Unit)

        push =
            Push(mockLoggingPush, mockGathererPush, mockPushInternal, sdkContext)

        push.registerPushToken(pushToken)

        verifyWithSuspend(exhaustive = false) {
            mockLoggingPush.registerPushToken(
                pushToken
            )
        }
    }

    @Test
    fun test_linkContact_onHoldState() = runTest {
        everySuspending {
            mockGathererPush.registerPushToken(
                pushToken
            )
        } returns SdkResult.Success(Unit)

        push =
            Push(mockLoggingPush, mockGathererPush, mockPushInternal, sdkContext)

        sdkContext.setSdkState(SdkState.onHold)
        push.registerPushToken(pushToken)

        verifyWithSuspend(exhaustive = false) {
            mockGathererPush.registerPushToken(
                pushToken
            )
        }
    }

    @Test
    fun test_linkContact_activeState() = runTest {
        everySuspending {
            mockPushInternal.registerPushToken(
                pushToken
            )
        } returns SdkResult.Success(Unit)

        push =
            Push(mockLoggingPush, mockGathererPush, mockPushInternal, sdkContext)

        sdkContext.setSdkState(SdkState.active)
        push.registerPushToken(pushToken)

        verifyWithSuspend(exhaustive = false) {
            mockPushInternal.registerPushToken(
                pushToken
            )
        }
    }

    @Test
    fun test_clearPushToken_inactiveState() = runTest {
        everySuspending {
            mockLoggingPush.clearPushToken()
        } returns SdkResult.Success(Unit)

        push =
            Push(mockLoggingPush, mockGathererPush, mockPushInternal, sdkContext)

        push.clearPushToken()

        verifyWithSuspend(exhaustive = false) {
            mockLoggingPush.clearPushToken()
        }
    }

    @Test
    fun test_clearPushToken_onHoldState() = runTest {
        everySuspending {
            mockGathererPush.clearPushToken()
        } returns SdkResult.Success(Unit)

        push =
            Push(mockLoggingPush, mockGathererPush, mockPushInternal, sdkContext)

        sdkContext.setSdkState(SdkState.onHold)
        push.clearPushToken()

        verifyWithSuspend(exhaustive = false) {
            mockGathererPush.clearPushToken()
        }
    }

    @Test
    fun test_clearPushToken_activeState() = runTest {
        everySuspending {
            mockPushInternal.clearPushToken()
        } returns SdkResult.Success(Unit)

        push =
            Push(mockLoggingPush, mockGathererPush, mockPushInternal, sdkContext)

        sdkContext.setSdkState(SdkState.active)
        push.clearPushToken()

        verifyWithSuspend(exhaustive = false) {
            mockPushInternal.clearPushToken()
        }
    }
}