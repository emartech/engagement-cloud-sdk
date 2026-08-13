package com.sap.ec.api.config

import com.sap.ec.core.collections.ThreadSafePersistentStoreApi
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class GathererConfigTests {
    private companion object {
        const val APP_CODE = "testAppCode"
    }

    private lateinit var gathererConfig: GathererConfig
    private lateinit var mockThreadSafePersistentStore: ThreadSafePersistentStoreApi<ConfigCall>

    @BeforeTest
    fun setUp() = runTest {
        mockThreadSafePersistentStore = mock(MockMode.autofill)
        everySuspend { mockThreadSafePersistentStore.add(any()) } returns Unit
        gathererConfig =
            GathererConfig(mockThreadSafePersistentStore, sdkLogger = mock(MockMode.autofill))
    }

    @Test
    fun testChangeApplicationCode_shouldStoreCall() = runTest {
        val expectedCall = ConfigCall.ChangeApplicationCode(APP_CODE)

        gathererConfig.changeApplicationCode(APP_CODE)

        verifySuspend { mockThreadSafePersistentStore.add(expectedCall) }
    }

    @Test
    fun testSetLanguage_shouldStoreCall() = runTest {
        val expectedCall = ConfigCall.SetLanguage("hu-HU")

        gathererConfig.setLanguage("hu-HU")

        verifySuspend { mockThreadSafePersistentStore.add(expectedCall) }
    }

    @Test
    fun testResetLanguage_shouldStoreCall() = runTest {
        val expectedCall = ConfigCall.ResetLanguage

        gathererConfig.resetLanguage()

        verifySuspend { mockThreadSafePersistentStore.add(expectedCall) }
    }
}