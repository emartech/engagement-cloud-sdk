package com.emarsys.api.inapp

import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.spy
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class InAppInternalTests {
    private lateinit var inAppContext: InAppContextApi
    private lateinit var inAppCallList: MutableList<InAppCall>

    @BeforeTest
    fun setup() {
        inAppContext = InAppContext(mutableListOf())
    }

    @Test
    fun testPause_shouldSetDND_true() = runTest {
        val inAppInternal = InAppInternal(InAppConfig(false), inAppContext)
        inAppInternal.isPaused shouldBe false

        inAppInternal.pause()

        inAppInternal.isPaused shouldBe true
    }

    @Test
    fun testResume_shouldSetDND_false() = runTest {
        val inAppInternal = InAppInternal(InAppConfig(true), inAppContext)

        inAppInternal.resume()

        inAppInternal.isPaused shouldBe false
    }

    @Test
    fun testActivate_shouldSetInAppDndToTheLastInAppCallValue_if_lastCallIsPause() = runTest {
        val lastInAppCall = InAppCall.Pause()
        inAppCallList = mutableListOf(InAppCall.Pause(), InAppCall.Resume(), lastInAppCall)
        val testInAppContext = InAppContext(inAppCallList)
        val inAppConfig: InAppConfigApi = InAppConfig(false)
        val spyInAppConfig = spy(inAppConfig)
        val inAppInternal = InAppInternal(spyInAppConfig, testInAppContext)

        inAppInternal.activate()

        inAppInternal.isPaused shouldBe true
        verify { spyInAppConfig.inAppDnd = true }
    }

    @Test
    fun testActivate_shouldSetInAppDndToTheLastInAppCallValue_if_lastCallIsResume() = runTest {
        val lastInAppCall = InAppCall.Resume()
        inAppCallList = mutableListOf(InAppCall.Resume(), InAppCall.Pause(), lastInAppCall)
        val testInAppContext = InAppContext(inAppCallList)
        val inAppConfig: InAppConfigApi = InAppConfig(true)
        val spyInAppConfig = spy(inAppConfig)
        val inAppInternal = InAppInternal(spyInAppConfig, testInAppContext)

        inAppInternal.activate()

        inAppInternal.isPaused shouldBe false
        verify { spyInAppConfig.inAppDnd = false }
    }

    @Test
    fun testActivate_shouldNotSetInAppDnd_if_callsListIsEmpty() = runTest {
        val mockInAppConfig = mock<InAppConfigApi>()

        val inAppInternal = InAppInternal(mockInAppConfig, inAppContext)

        inAppInternal.activate()

        verify(VerifyMode.exactly(0)) { mockInAppConfig.inAppDnd = any<Boolean>() }
    }

}