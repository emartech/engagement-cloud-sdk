package com.emarsys.api.push

import dev.mokkery.MockMode
import dev.mokkery.answering.returns
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
class JSPushTests {
    private companion object {
        val testException = Exception("testException")
        val testFailedResult = Result.failure<Unit>(testException)
        val testSuccessResult = Result.success<Unit>(Unit)
    }

    private lateinit var jsPush: JSPush
    private lateinit var mockPushApi: PushApi

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        mockPushApi = mock(MockMode.autoUnit)
        jsPush = JSPush(mockPushApi)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun registerPushToken_shouldCall_registerPushTokenOnPushApi() = runTest {
        val testPushToken = "testPushToken"
        everySuspend { mockPushApi.registerPushToken(testPushToken) } returns testSuccessResult

        jsPush.registerPushToken(testPushToken)

        verifySuspend { mockPushApi.registerPushToken(testPushToken) }
    }

    @Test
    fun registerPushToken_shouldThrowException_ifRegisterPushToken_fails() = runTest {
        val testPushToken = "testPushToken"
        everySuspend { mockPushApi.registerPushToken(testPushToken) } returns testFailedResult

        shouldThrow<Exception> { jsPush.registerPushToken(testPushToken) }
    }

    @Test
    fun clearPushToken_shouldCall_clearPushTokenOnPushApi() = runTest {
        everySuspend { mockPushApi.clearPushToken() } returns testSuccessResult

        jsPush.clearPushToken()

        verifySuspend { mockPushApi.clearPushToken() }
    }

    @Test
    fun clearPushToken_shouldThrowException_ifClearPushToken_fails() = runTest {
        everySuspend { mockPushApi.clearPushToken() } returns testFailedResult

        shouldThrow<Exception> { jsPush.clearPushToken() }
    }

    @Test
    fun getPushToken_shouldCall_getPushTokenOnPushApi() = runTest {
        val token = "testPushToken"
        val testSuccessResultWithString = Result.success<String>(token)
        everySuspend { mockPushApi.getPushToken() } returns testSuccessResultWithString

        val result = jsPush.getPushToken()

        result shouldBe token
    }

    @Test
    fun getPushToken_shouldThrowException_ifGetPushToken_fails() = runTest {
        val testFailedResult = Result.failure<String>(testException)
        everySuspend { mockPushApi.getPushToken() } returns testFailedResult

        shouldThrow<Exception> { jsPush.clearPushToken() }
    }
}