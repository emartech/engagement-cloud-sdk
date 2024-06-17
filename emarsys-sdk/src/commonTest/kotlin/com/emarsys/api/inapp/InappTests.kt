
package com.emarsys.api.inapp

import com.emarsys.api.AppEvent
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
import dev.mokkery.verify
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InappTests {
    private companion object {
        val testException = Exception()
        val testEvents = MutableSharedFlow<AppEvent>()
    }

    private lateinit var mockLoggingInApp: InAppInstance
    private lateinit var mockGathererInApp: InAppInstance
    private lateinit var mockInAppInternal: InAppInstance
    private lateinit var sdkContext: SdkContextApi
    private lateinit var inApp: InApp<InAppInstance, InAppInstance, InAppInstance>

    init {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @BeforeTest
    fun setup() = runTest {
        mockLoggingInApp = mock()
        mockGathererInApp = mock()
        mockInAppInternal = mock()

        sdkContext = SdkContext(
            StandardTestDispatcher(),
            DefaultUrls("", "", "", "", "", "", ""),
            LogLevel.Error,
            mutableSetOf()
        )

        everySuspend { mockLoggingInApp.activate() } returns Unit
        everySuspend { mockGathererInApp.activate() } returns Unit
        everySuspend { mockInAppInternal.activate() } returns Unit

        inApp = InApp(mockLoggingInApp, mockGathererInApp, mockInAppInternal, sdkContext)
        inApp.registerOnContext()
    }

    @Test
    fun testIsPaused_when_inactiveState() = runTest {
        every { mockLoggingInApp.isPaused } returns false

        inApp.isPaused shouldBe false
        verify { mockLoggingInApp.isPaused }
    }

    @Test
    fun testIsPaused_when_onHoldState() = runTest {
        every { mockGathererInApp.isPaused } returns true

        sdkContext.setSdkState(SdkState.onHold)
        inApp.isPaused shouldBe true

        verify { mockGathererInApp.isPaused }
    }

    @Test
    fun testIsPaused_when_activeState() = runTest {
        every { mockInAppInternal.isPaused } returns true

        sdkContext.setSdkState(SdkState.active)
        inApp.isPaused shouldBe true

        verify { mockInAppInternal.isPaused }
    }

    @Test
    fun testEvents_when_inactiveState() = runTest {
        every { mockLoggingInApp.events } returns testEvents

        inApp.events

        verify { mockLoggingInApp.events }
    }

    @Test
    fun testEvents_when_onHoldState() = runTest {
        every { mockGathererInApp.events } returns testEvents
        sdkContext.setSdkState(SdkState.onHold)

        inApp.events

        verify { mockGathererInApp.events }
    }

    @Test
    fun testEvents_when_activeState() = runTest {
        every { mockInAppInternal.events } returns testEvents
        sdkContext.setSdkState(SdkState.active)

        inApp.events

        verify { mockInAppInternal.events }
    }

    @Test
    fun testPause_when_inactiveState() = runTest {
        everySuspend { mockLoggingInApp.pause() } returns Unit

        inApp.pause()

        verifySuspend { mockLoggingInApp.pause() }
    }

    @Test
    fun testPause_when_onHoldState() = runTest {
        everySuspend { mockGathererInApp.pause() } returns Unit

        sdkContext.setSdkState(SdkState.onHold)

        inApp.pause()

        verifySuspend { mockGathererInApp.pause() }
    }

    @Test
    fun testPause_when_activeState() = runTest {
        everySuspend { mockInAppInternal.pause() } returns Unit
        sdkContext.setSdkState(SdkState.active)

        inApp.pause()

        verifySuspend { mockInAppInternal.pause() }
    }

    @Test
    fun testPause_when_activeState_throws() = runTest {
        everySuspend { mockInAppInternal.pause() } throws testException

        sdkContext.setSdkState(SdkState.active)

        val result = inApp.pause()

        result.exceptionOrNull() shouldBe testException
    }

    @Test
    fun testResume_when_inactiveState() = runTest {
        everySuspend { mockLoggingInApp.resume() } returns Unit

        inApp.resume()

        verifySuspend { mockLoggingInApp.resume() }
    }

    @Test
    fun testResume_when_onHoldState() = runTest {
        everySuspend { mockGathererInApp.resume() } returns Unit

        sdkContext.setSdkState(SdkState.onHold)

        inApp.resume()

        verifySuspend { mockGathererInApp.resume() }
    }

    @Test
    fun testResume_when_activeState() = runTest {
        everySuspend { mockInAppInternal.resume() } returns Unit
        sdkContext.setSdkState(SdkState.active)

        inApp.resume()

        verifySuspend { mockInAppInternal.resume() }
    }

    @Test
    fun testResume_when_activeState_throws() = runTest {
        everySuspend { mockInAppInternal.resume() } throws testException

        sdkContext.setSdkState(SdkState.active)

        val result = inApp.resume()

        result.exceptionOrNull() shouldBe testException
    }

}