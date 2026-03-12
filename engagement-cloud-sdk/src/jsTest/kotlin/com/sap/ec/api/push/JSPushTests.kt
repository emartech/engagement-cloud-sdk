package com.sap.ec.api.push

import com.sap.ec.mobileengage.push.JsPushWrapperApi
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
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
class JSPushTests {
    private lateinit var jsPush: JSPush
    private lateinit var mockJsPushWrapperApi: JsPushWrapperApi

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        mockJsPushWrapperApi = mock(MockMode.autoUnit)
        jsPush = JSPush(mockJsPushWrapperApi)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun subscribe_shouldDelegateTo_jsPushWrapperApi() = runTest {
        everySuspend { mockJsPushWrapperApi.subscribe() } returns Result.success(Unit)

        jsPush.subscribe()

        verifySuspend { mockJsPushWrapperApi.subscribe() }
    }

    @Test
    fun unsubscribe_shouldDelegateTo_jsPushWrapperApi() = runTest {
        everySuspend { mockJsPushWrapperApi.unsubscribe() } returns Result.success(Unit)

        jsPush.unsubscribe()

        verifySuspend { mockJsPushWrapperApi.unsubscribe() }
    }

    @Test
    fun isSubscribed_shouldDelegateTo_jsPushWrapperApi() = runTest {
        everySuspend { mockJsPushWrapperApi.isSubscribed() } returns true

        val result = jsPush.isSubscribed()

        result shouldBe true
        verifySuspend { mockJsPushWrapperApi.isSubscribed() }
    }

    @Test
    fun getPermissionState_shouldDelegateTo_jsPushWrapperApi() = runTest {
        everySuspend { mockJsPushWrapperApi.getPermissionState() } returns "granted"

        val result = jsPush.getPermissionState()

        result shouldBe "granted"
        verifySuspend { mockJsPushWrapperApi.getPermissionState() }
    }
}
