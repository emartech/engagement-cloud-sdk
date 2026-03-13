package com.sap.ec.mobileengage.push

import com.sap.ec.core.exceptions.SdkException
import com.sap.ec.core.log.Logger
import com.sap.ec.core.permission.PermissionHandlerApi
import com.sap.ec.core.storage.StringStorageApi
import com.sap.ec.mobileengage.push.serviceworker.ServiceWorkerManagerApi
import dev.mokkery.MockMode
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class PushServiceTests {

    private lateinit var pushService: PushService
    private lateinit var mockServiceWorkerManager: ServiceWorkerManagerApi
    private lateinit var mockWebPermissionHandler: PermissionHandlerApi
    private lateinit var mockStorage: StringStorageApi
    private lateinit var mockLogger: Logger

    @BeforeTest
    fun setup() {
        mockServiceWorkerManager = mock(MockMode.autofill)
        mockWebPermissionHandler = mock(MockMode.autofill)
        mockStorage = mock(MockMode.autofill)
        mockLogger = mock(MockMode.autofill)
        pushService = PushService(
            mockServiceWorkerManager,
            mockWebPermissionHandler,
            mockStorage,
            mockLogger
        )
    }

    @Test
    fun serviceWorkerUnavailableException_shouldBeSdkException() {
        val exception = SdkException.ServiceWorkerUnavailableException("Service workers are not supported in this browser.")
        exception.shouldBeInstanceOf<SdkException>()
        exception.message shouldBe "Service workers are not supported in this browser."
    }

    @Test
    fun pushManagerUnavailableException_shouldBeSdkException() {
        val exception = SdkException.PushManagerUnavailableException("Push API is not supported in this browser.")
        exception.shouldBeInstanceOf<SdkException>()
        exception.message shouldBe "Push API is not supported in this browser."
    }
}
