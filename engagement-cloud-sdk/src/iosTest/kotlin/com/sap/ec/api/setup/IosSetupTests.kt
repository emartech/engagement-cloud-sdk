package com.sap.ec.api.setup

import com.sap.ec.IosEngagementCloudSDKConfig
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.assertions.throwables.shouldThrow
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class IosSetupTests {
    private lateinit var iosSetup: IosSetupApi
    private lateinit var mockSetup: SetupApi

    @BeforeTest
    fun setup() {
        mockSetup = mock(MockMode.autofill)
        iosSetup = IosSetup(mockSetup)
    }

    @Test
    fun enableTracking_shouldDelegate_toTheSameMethod_onSetupApi() = runTest {
        val iosConfig = IosEngagementCloudSDKConfig("ABC-123")
        val onContactLinkingFailed: OnContactLinkingFailed = { onSuccess, onError -> }
        everySuspend { mockSetup.enable(iosConfig, any()) } returns Result.success(Unit)

        iosSetup.enable(iosConfig, onContactLinkingFailed = onContactLinkingFailed)

        verifySuspend { mockSetup.enable(iosConfig, any()) }
    }

    @Test
    fun enableTracking_shouldThrow_whenSetupApi_returnsFailure() = runTest {
        val iosConfig = IosEngagementCloudSDKConfig("ABC-123")
        val onContactLinkingFailed: OnContactLinkingFailed = { onSuccess, onError -> }
        everySuspend { mockSetup.enable(iosConfig, any()) } returns Result.failure(
            RuntimeException(
                "Enable failed"
            )
        )

        shouldThrow<RuntimeException> {
            iosSetup.enable(iosConfig, onContactLinkingFailed = onContactLinkingFailed)
        }

        verifySuspend { mockSetup.enable(iosConfig, any()) }
    }

    @Test
    fun disableTracking_shouldDelegate_toTheSameMethod_onSetupApi() = runTest {
        everySuspend { mockSetup.disable() } returns Result.success(Unit)


        iosSetup.disable()

        verifySuspend { mockSetup.disable() }
    }

    @Test
    fun disableTracking_shouldThrow_whenSetupApi_returnsFailure() = runTest {
        everySuspend { mockSetup.disable() } returns Result.failure(RuntimeException("Disable failed"))


        shouldThrow<RuntimeException> { iosSetup.disable() }

        verifySuspend { mockSetup.disable() }
    }
}