package com.sap.ec.mobileengage.push

import com.sap.ec.core.log.Logger
import com.sap.ec.core.storage.StringStorageApi
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class PushServiceTests {

    private lateinit var pushService: PushService
    private lateinit var mockStorage: StringStorageApi
    private lateinit var mockLogger: Logger

//    @BeforeTest
//    fun setup() {
//        mockPushServiceContext = mock(MockMode.autoUnit)
//        mockStorage = mock(MockMode.autoUnit)
//        mockLogger = mock(MockMode.autoUnit)
//        pushService = PushService(mockPushServiceContext, mockStorage, mockLogger)
//    }
//
//    @Test
//    fun subscribeForPushMessages_shouldReturnNull_whenServiceWorkerOptionsIsNull() = runTest {
//        everySuspend { mockPushServiceContext.serviceWorkerOptions } returns null
//
//        val result = pushService.subscribe()
//
//        result shouldBe null
//        everySuspend { mockPushServiceContext.isSubscribed } returns false
//        mockPushServiceContext.isSubscribed shouldBe false
//    }
//
//    @Test
//    fun unsubscribe_shouldSetIsSubscribedToFalse_andClearToken() = runTest {
//        everySuspend { mockPushServiceContext.registration } returns null
//
//        pushService.unsubscribe()
//
//        verifySuspend { mockStorage.put("emsPushToken", null) }
//    }
}
