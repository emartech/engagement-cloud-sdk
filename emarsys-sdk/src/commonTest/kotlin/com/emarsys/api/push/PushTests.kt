package com.emarsys.api.push

import com.emarsys.api.SdkState
import com.emarsys.context.DefaultUrls
import com.emarsys.context.SdkContext
import com.emarsys.context.SdkContextApi
import com.emarsys.core.log.LogLevel
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
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
class PushTests {
    private companion object {
        const val PUSH_TOKEN = "testPushToken"
    }

    private val mainDispatcher = StandardTestDispatcher()

    init {
        Dispatchers.setMain(mainDispatcher)
    }

    private lateinit var mockLoggingPush: PushInstance
    private lateinit var mockGathererPush: PushInstance
    private lateinit var mockPushInternal: PushInstance
    private lateinit var sdkContext: SdkContextApi
    private lateinit var push: Push<PushInstance, PushInstance, PushInstance>

    @BeforeTest
    fun setup() = runTest {
        mockLoggingPush = mock()
        mockGathererPush = mock()
        mockPushInternal = mock()

        sdkContext = SdkContext(
            StandardTestDispatcher(),
            mainDispatcher,
            DefaultUrls("", "", "", "", "", "", ""),
            LogLevel.Error,
            mutableSetOf()
        )

        everySuspend { mockLoggingPush.activate() } returns Unit
        everySuspend { mockGathererPush.activate() } returns Unit
        everySuspend { mockPushInternal.activate() } returns Unit

        push =
            Push(mockLoggingPush, mockGathererPush, mockPushInternal, sdkContext)
        push.registerOnContext()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()

    }

    @Test
    fun testRegisterPushToken_inactiveState() = runTest {
        everySuspend {
            mockLoggingPush.registerPushToken(
                PUSH_TOKEN
            )
        } returns Unit

        push.registerPushToken(PUSH_TOKEN)

        verifySuspend {
            mockLoggingPush.registerPushToken(
                PUSH_TOKEN
            )
        }
    }

    @Test
    fun testRegisterPushToken_onHoldState() = runTest {
        everySuspend {
            mockGathererPush.registerPushToken(
                PUSH_TOKEN
            )
        } returns Unit

        sdkContext.setSdkState(SdkState.onHold)
        push.registerPushToken(PUSH_TOKEN)

        verifySuspend {
            mockGathererPush.registerPushToken(
                PUSH_TOKEN
            )
        }
    }

    @Test
    fun testRegisterPushToken_activeState() = runTest {
        everySuspend {
            mockPushInternal.registerPushToken(
                PUSH_TOKEN
            )
        } returns Unit

        sdkContext.setSdkState(SdkState.active)
        push.registerPushToken(PUSH_TOKEN)

        verifySuspend {
            mockPushInternal.registerPushToken(
                PUSH_TOKEN
            )
        }
    }

    @Test
    fun testRegisterPushToken_activeState_when_throws() = runTest {
        val expectedException = Exception()
        everySuspend {
            mockPushInternal.registerPushToken(
                PUSH_TOKEN
            )
        } throws expectedException

        sdkContext.setSdkState(SdkState.active)
        val result = push.registerPushToken(PUSH_TOKEN)

        result.onFailure {
            it shouldBe expectedException
        }
    }

    @Test
    fun testClearPushToken_inactiveState() = runTest {
        everySuspend {
            mockLoggingPush.clearPushToken()
        } returns Unit

        push.clearPushToken()

        verifySuspend {
            mockLoggingPush.clearPushToken()
        }
    }

    @Test
    fun testClearPushToken_onHoldState() = runTest {
        everySuspend {
            mockGathererPush.clearPushToken()
        } returns Unit

        sdkContext.setSdkState(SdkState.onHold)
        push.clearPushToken()

        verifySuspend {
            mockGathererPush.clearPushToken()
        }
    }

    @Test
    fun testClearPushToken_activeState() = runTest {
        everySuspend {
            mockPushInternal.clearPushToken()
        } returns Unit

        sdkContext.setSdkState(SdkState.active)
        push.clearPushToken()

        verifySuspend {
            mockPushInternal.clearPushToken()
        }
    }

    @Test
    fun testClearPushToken_activeState_when_throws() = runTest {
        val expectedException = Exception()
        everySuspend {
            mockPushInternal.clearPushToken()
        } throws expectedException

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
