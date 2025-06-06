package com.emarsys

import com.emarsys.core.exceptions.SdkException
import com.emarsys.core.log.Logger
import com.emarsys.di.SdkKoinIsolationContext.koin
import com.emarsys.fake.FakeLogger
import io.kotest.assertions.throwables.shouldThrow
import kotlinx.coroutines.test.runTest
import org.koin.core.Koin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.test.KoinTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class EmarsysTests: KoinTest {
    override fun getKoin(): Koin = koin

    private lateinit var testModule: Module

    @BeforeTest
    fun setUp() {
        testModule = module {
            single<Logger> { FakeLogger() }

        }
        koin.loadModules(listOf(testModule))
    }

    @Test
    fun testEnableTracking_shouldValidateConfig() = runTest {
        val config = TestEmarsysConfig(applicationCode = "null")
        shouldThrow<SdkException.InvalidApplicationCodeException> {
            Emarsys.enableTracking(config)
        }
    }
}