package com.sap.ec.api.inapp

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.assertions.throwables.shouldThrow
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
class JSInAppTests {

    private lateinit var jsInApp: JSInAppApi
    private lateinit var mockInApp: InAppApi

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        mockInApp = mock()
        every { mockInApp.isPaused } returns false
        jsInApp = JSInApp(mockInApp)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun isPaused_shouldCall_inAppApi() = runTest {
        jsInApp.isPaused shouldBe false
    }

    @Test
    fun pause_shouldCall_pause_onInAppApi() = runTest {
        everySuspend { mockInApp.pause() } returns Result.success(Unit)

        jsInApp.pause()

        verifySuspend { mockInApp.pause() }
    }

    @Test
    fun pause_shouldThrowException_ifPauseFails_onApi() = runTest {
        everySuspend { mockInApp.pause() } returns Result.failure(Exception())

        shouldThrow<Exception> { jsInApp.pause() }
    }

    @Test
    fun resume_shouldCall_resume_onInAppApi() = runTest {
        everySuspend { mockInApp.resume() } returns Result.success(Unit)

        jsInApp.resume()

        verifySuspend { mockInApp.resume() }
    }

    @Test
    fun resume_shouldThrowException_ifResumeFails_onApi() = runTest {
        everySuspend { mockInApp.resume() } returns Result.failure(Exception())

        shouldThrow<Exception> { jsInApp.resume() }
    }
}