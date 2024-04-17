
package com.emarsys.api.inapp

import com.emarsys.api.AppEvent
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
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InappTests : TestsWithMocks() {
    private companion object {
        val testException = Exception()
        val testEvents = MutableSharedFlow<AppEvent>()
    }

    override fun setUpMocks() = injectMocks(mocker)

    @Mock
    lateinit var mockLoggingInApp: InAppInstance

    @Mock
    lateinit var mockGathererInApp: InAppInstance

    @Mock
    lateinit var mockInAppInternal: InAppInstance

    private lateinit var sdkContext: SdkContextApi

    private lateinit var inApp: InApp<InAppInstance, InAppInstance, InAppInstance>

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

        everySuspending { mockLoggingInApp.activate() } returns Unit
        everySuspending { mockGathererInApp.activate() } returns Unit
        everySuspending { mockInAppInternal.activate() } returns Unit

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

        verify(exhaustive = false) { mockGathererInApp.isPaused }
    }

    @Test
    fun testIsPaused_when_activeState() = runTest {
        every { mockInAppInternal.isPaused } returns true

        sdkContext.setSdkState(SdkState.active)
        inApp.isPaused shouldBe true

        verify(exhaustive = false) { mockInAppInternal.isPaused }
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

        verify(exhaustive = false) { mockGathererInApp.events }
    }

    @Test
    fun testEvents_when_activeState() = runTest {
        every { mockInAppInternal.events } returns testEvents
        sdkContext.setSdkState(SdkState.active)

        inApp.events

        verify(exhaustive = false) { mockInAppInternal.events }
    }

    @Test
    fun testPause_when_inactiveState() = runTest {
        everySuspending { mockLoggingInApp.pause() } returns Unit

        inApp.pause()

        verifyWithSuspend(exhaustive = false) { mockLoggingInApp.pause() }
    }

    @Test
    fun testPause_when_onHoldState() = runTest {
        everySuspending { mockGathererInApp.pause() } returns Unit

        sdkContext.setSdkState(SdkState.onHold)

        inApp.pause()

        verifyWithSuspend(exhaustive = false) { mockGathererInApp.pause() }
    }

    @Test
    fun testPause_when_activeState() = runTest {
        everySuspending { mockInAppInternal.pause() } returns Unit
        sdkContext.setSdkState(SdkState.active)

        inApp.pause()

        verifyWithSuspend(exhaustive = false) { mockInAppInternal.pause() }
    }

    @Test
    fun testPause_when_activeState_throws() = runTest {
        everySuspending { mockInAppInternal.pause() } runs {
            throw testException
        }

        sdkContext.setSdkState(SdkState.active)

        val result = inApp.pause()

        result.exceptionOrNull() shouldBe testException
    }

    @Test
    fun testResume_when_inactiveState() = runTest {
        everySuspending { mockLoggingInApp.resume() } returns Unit

        inApp.resume()

        verifyWithSuspend(exhaustive = false) { mockLoggingInApp.resume() }
    }

    @Test
    fun testResume_when_onHoldState() = runTest {
        everySuspending { mockGathererInApp.resume() } returns Unit

        sdkContext.setSdkState(SdkState.onHold)

        inApp.resume()

        verifyWithSuspend(exhaustive = false) { mockGathererInApp.resume() }
    }

    @Test
    fun testResume_when_activeState() = runTest {
        everySuspending { mockInAppInternal.resume() } returns Unit
        sdkContext.setSdkState(SdkState.active)

        inApp.resume()

        verifyWithSuspend(exhaustive = false) { mockInAppInternal.resume() }
    }

    @Test
    fun testResume_when_activeState_throws() = runTest {
        everySuspending { mockInAppInternal.resume() } runs {
            throw testException
        }

        sdkContext.setSdkState(SdkState.active)

        val result = inApp.resume()

        result.exceptionOrNull() shouldBe testException
    }

}