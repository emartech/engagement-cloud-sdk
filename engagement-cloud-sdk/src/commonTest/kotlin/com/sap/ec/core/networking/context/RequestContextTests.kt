package com.sap.ec.core.networking.context

import com.sap.ec.core.storage.StringStorageApi
import com.sap.ec.di.SdkKoinIsolationContext.koin
import com.sap.ec.fake.FakeStringStorage
import com.sap.ec.util.JsonUtil
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import org.koin.core.Koin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.test.KoinTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class RequestContextTests: KoinTest {
    override fun getKoin(): Koin = koin

    private lateinit var testModule: Module
    private lateinit var requestContext: RequestContextApi

    companion object {
        const val CONTACT_TOKEN = "testContactToken"
        const val REFRESH_TOKEN = "testRefreshToken"
    }

    @BeforeTest
    fun setUp() {
        testModule = module {
            single<StringStorageApi> { FakeStringStorage() }
            single<Json> { JsonUtil.json }
        }
        koin.loadModules(listOf(testModule))

        requestContext = RequestContext()
    }

    @AfterTest
    fun tearDown() {
        koin.unloadModules(listOf(testModule))
    }

    @Test
    fun testClearTokens() {
        requestContext.contactToken = CONTACT_TOKEN
        requestContext.refreshToken = REFRESH_TOKEN

        requestContext.contactToken shouldBe CONTACT_TOKEN
        requestContext.refreshToken shouldBe REFRESH_TOKEN

        requestContext.clearTokens()

        requestContext.contactToken shouldBe null
        requestContext.refreshToken shouldBe null
    }

}