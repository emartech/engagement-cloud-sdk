package com.emarsys.api.push

import com.emarsys.api.SdkState
import com.emarsys.context.DefaultUrls
import com.emarsys.context.SdkContext
import com.emarsys.context.SdkContextApi
import com.emarsys.core.log.LogLevel
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
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
        const val PUSH_TOKEN = "testPushToken"
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
            LogLevel.Error,
            mutableSetOf()
        )

        everySuspending { mockLoggingPush.activate() } returns Unit
        everySuspending { mockGathererPush.activate() } returns Unit
        everySuspending { mockPushInternal.activate() } returns Unit

        every {
            mockLoggingPush.notificationEvents
        } returns MutableSharedFlow()
        every {
            mockGathererPush.notificationEvents
        } returns MutableSharedFlow()
        every {
            mockPushInternal.notificationEvents
        } returns MutableSharedFlow()

        push =
            Push(mockLoggingPush, mockGathererPush, mockPushInternal, sdkContext)
        push.registerOnContext()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        mocker.reset()
    }

    @Test
    fun testRegisterPushToken_inactiveState() = runTest {
        everySuspending {
            mockLoggingPush.registerPushToken(
                PUSH_TOKEN
            )
        } returns Unit

        push.registerPushToken(PUSH_TOKEN)

        verifyWithSuspend(exhaustive = false) {
            mockLoggingPush.registerPushToken(
                PUSH_TOKEN
            )
        }
    }

    @Test
    fun testRegisterPushToken_onHoldState() = runTest {
        everySuspending {
            mockGathererPush.registerPushToken(
                PUSH_TOKEN
            )
        } returns Unit

        sdkContext.setSdkState(SdkState.onHold)
        push.registerPushToken(PUSH_TOKEN)

        verifyWithSuspend(exhaustive = false) {
            mockGathererPush.registerPushToken(
                PUSH_TOKEN
            )
        }
    }

    @Test
    fun testRegisterPushToken_activeState() = runTest {
        everySuspending {
            mockPushInternal.registerPushToken(
                PUSH_TOKEN
            )
        } returns Unit

        sdkContext.setSdkState(SdkState.active)
        push.registerPushToken(PUSH_TOKEN)

        verifyWithSuspend(exhaustive = false) {
            mockPushInternal.registerPushToken(
                PUSH_TOKEN
            )
        }
    }

    @Test
    fun testRegisterPushToken_activeState_when_throws() = runTest {
        val expectedException = Exception()
        everySuspending {
            mockPushInternal.registerPushToken(
                PUSH_TOKEN
            )
        } runs {
            throw expectedException
        }

        sdkContext.setSdkState(SdkState.active)
        val result = push.registerPushToken(PUSH_TOKEN)

        result.onFailure {
            it shouldBe expectedException
        }
    }

    @Test
    fun testClearPushToken_inactiveState() = runTest {
        everySuspending {
            mockLoggingPush.clearPushToken()
        } returns Unit

        push.clearPushToken()

        verifyWithSuspend(exhaustive = false) {
            mockLoggingPush.clearPushToken()
        }
    }

    @Test
    fun testClearPushToken_onHoldState() = runTest {
        everySuspending {
            mockGathererPush.clearPushToken()
        } returns Unit

        sdkContext.setSdkState(SdkState.onHold)
        push.clearPushToken()

        verifyWithSuspend(exhaustive = false) {
            mockGathererPush.clearPushToken()
        }
    }

    @Test
    fun testClearPushToken_activeState() = runTest {
        everySuspending {
            mockPushInternal.clearPushToken()
        } returns Unit

        sdkContext.setSdkState(SdkState.active)
        push.clearPushToken()

        verifyWithSuspend(exhaustive = false) {
            mockPushInternal.clearPushToken()
        }
    }

    @Test
    fun testClearPushToken_activeState_when_throws() = runTest {
        val expectedException = Exception()
        everySuspending {
            mockPushInternal.clearPushToken()
        } runs {
            throw expectedException
        }

        sdkContext.setSdkState(SdkState.active)
        val result = push.clearPushToken()

        result.onFailure {
            it shouldBe expectedException
        }
    }

    @Test
    fun testPushToken_inactiveState() = runTest {
        every {
            mockLoggingPush.pushToken
        } returns null

        val result = push.pushToken

        result.onSuccess {
            it shouldBe null
        }
    }

    @Test
    fun testPushToken_onHoldState() = runTest {
        every {
            mockGathererPush.pushToken
        } returns PUSH_TOKEN

        sdkContext.setSdkState(SdkState.onHold)

        val result = push.pushToken

        result.onSuccess {
            it shouldBe PUSH_TOKEN
        }
    }

    @Test
    fun testPushToken_activeState() = runTest {
        every {
            mockPushInternal.pushToken
        } returns PUSH_TOKEN

        sdkContext.setSdkState(SdkState.active)

        val result = push.pushToken

        result.onSuccess {
            it shouldBe PUSH_TOKEN
        }
    }
}
