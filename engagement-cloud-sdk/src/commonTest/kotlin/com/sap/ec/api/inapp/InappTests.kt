package com.sap.ec.api.inapp

import com.sap.ec.api.SdkState
import com.sap.ec.context.SdkContextApi
import dev.mokkery.MockMode
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InappTests {
    private companion object {
        val TEST_EXCEPTION = Exception()
    }

    private lateinit var mockLoggingInApp: InAppInstance
    private lateinit var mockGathererInApp: InAppInstance
    private lateinit var mockInAppInternal: InAppInstance
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var inApp: InApp<InAppInstance, InAppInstance, InAppInstance>


    @BeforeTest
    fun setup() {
        val mainDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(mainDispatcher)
        mockLoggingInApp = mock(MockMode.autofill)
        mockGathererInApp = mock(MockMode.autofill)
        mockInAppInternal = mock(MockMode.autofill)

        mockSdkContext = mock(MockMode.autofill)
        every { mockSdkContext.sdkDispatcher } returns mainDispatcher

        inApp = InApp(
            mockLoggingInApp,
            mockGathererInApp,
            mockInAppInternal,
            mockSdkContext
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testIsPaused_when_inactiveState() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Initialized)
        every { mockLoggingInApp.isPaused } returns false

        inApp.registerOnContext()

        inApp.isPaused shouldBe false
        verify { mockLoggingInApp.isPaused }
    }

    @Test
    fun testIsPaused_when_onHoldState() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.OnHold)
        every { mockGathererInApp.isPaused } returns true

        inApp.registerOnContext()

        advanceUntilIdle()

        inApp.isPaused shouldBe true

        verify { mockGathererInApp.isPaused }
    }

    @Test
    fun testIsPaused_when_activeState() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Active)
        every { mockInAppInternal.isPaused } returns true

        inApp.registerOnContext()

        advanceUntilIdle()
        inApp.isPaused shouldBe true

        verify { mockInAppInternal.isPaused }
    }

    @Test
    fun testPause_when_inactiveState() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Initialized)

        inApp.registerOnContext()

        advanceUntilIdle()

        inApp.pause()

        verifySuspend { mockLoggingInApp.pause() }
    }

    @Test
    fun testPause_when_onHoldState() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.OnHold)

        inApp.registerOnContext()

        advanceUntilIdle()

        inApp.pause()

        verifySuspend { mockGathererInApp.pause() }
    }

    @Test
    fun testPause_when_activeState() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Active)

        inApp.registerOnContext()

        advanceUntilIdle()

        inApp.pause()

        verifySuspend { mockInAppInternal.pause() }
    }

    @Test
    fun testPause_when_activeState_throws() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Active)
        everySuspend { mockInAppInternal.pause() } throws TEST_EXCEPTION

        inApp.registerOnContext()

        advanceUntilIdle()

        val result = inApp.pause()

        result.exceptionOrNull() shouldBe TEST_EXCEPTION
    }

    @Test
    fun testResume_when_inactiveState() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Initialized)

        inApp.registerOnContext()

        advanceUntilIdle()

        inApp.resume()

        advanceUntilIdle()

        verifySuspend { mockLoggingInApp.resume() }
    }

    @Test
    fun testResume_when_onHoldState() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.OnHold)

        inApp.registerOnContext()

        advanceUntilIdle()

        inApp.resume()

        advanceUntilIdle()

        verifySuspend { mockGathererInApp.resume() }
    }

    @Test
    fun testResume_when_activeState() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Active)

        inApp.registerOnContext()

        advanceUntilIdle()

        inApp.resume()

        verifySuspend { mockInAppInternal.resume() }
    }

    @Test
    fun testResume_when_activeState_throws() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Active)
        everySuspend { mockInAppInternal.resume() } throws TEST_EXCEPTION

        inApp.registerOnContext()

        advanceUntilIdle()

        val result = inApp.resume()

        result.exceptionOrNull() shouldBe TEST_EXCEPTION
    }

}