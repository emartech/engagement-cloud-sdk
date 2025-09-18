package com.emarsys

import com.emarsys.api.setup.SetupApi
import com.emarsys.di.SdkKoinIsolationContext.koin
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.koin.core.Koin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.test.KoinTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class EmarsysTests : KoinTest {
    override fun getKoin(): Koin = koin

    private companion object {
        val config = TestEmarsysConfig(applicationCode = "testConfig")
        val testException = Exception("failure")
    }

    private lateinit var testModule: Module
    private lateinit var mockSetup: SetupApi

    @BeforeTest
    fun setUp() {
        testModule = module {
            single<SetupApi> { mockSetup }
        }
        koin.loadModules(listOf(testModule))
    }

    @Test
    fun testEnableTracking_shouldReturn_successFrom_setupApi_call() = runTest {
        mockSetup = mock {
            everySuspend { enableTracking(config) } returns Result.success(Unit)
        }

        val result = Emarsys.enableTracking(config)

        result.isSuccess shouldBe true
        result.exceptionOrNull() shouldBe null
    }

    @Test
    fun testEnableTracking_shouldReturn_failureFrom_setupApi_call() = runTest {
        mockSetup = mock {
            everySuspend { enableTracking(config) } returns Result.failure(testException)
        }

        val result = Emarsys.enableTracking(config)

        result.exceptionOrNull() shouldBe testException
    }

    @Test
    fun testDisableTracking_shouldReturn_successFrom_setupApi_call() = runTest {
        mockSetup = mock {
            everySuspend { disableTracking() } returns Result.success(Unit)
        }

        val result = Emarsys.disableTracking()

        result.isSuccess shouldBe true
        result.exceptionOrNull() shouldBe null
    }

    @Test
    fun testDisableTracking_shouldReturn_failureFrom_setupApi_call() = runTest {
        mockSetup = mock {
            everySuspend { disableTracking() } returns Result.failure(testException)
        }

        val result = Emarsys.disableTracking()

        result.exceptionOrNull() shouldBe testException
    }
}